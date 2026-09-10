#!/usr/bin/env python3
"""
GeoJSON(행정경계) -> Android Canvas 용 Path 데이터(JSON) 변환기.

산출물:
  app/src/main/assets/map/provinces.json          (줌아웃: 17 시도, 섬 정리 + 가장자리 스무딩)
  app/src/main/assets/map/sigungu/<code>.json     (줌인: 해당 시도의 시군구, 실제 가장자리 유지)

좌표계: 경위도 -> 등거리 근사투영(위도보정) -> 대상 viewBox(px)로 정규화.
"""
import json, math, os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "app/src/main/assets/map")
PROV_SRC = "/tmp/kr_prov.json"
MUNI_SRC = "/tmp/kr_muni.json"

MEAN_LAT = 36.5
KX = math.cos(math.radians(MEAN_LAT))  # 경도 축소 비율

# 특별시/광역시: 작은 구가 많아 정밀 단순화 필요(경계 맞물림). 세종(29)은 단일지역이라 제외.
METRO_CODES = {"11", "21", "22", "23", "24", "25", "26"}
# 줌인(시군구 분할)을 하지 않는 시도: 광역시 7곳 + 세종.
# 이 지역들은 줌아웃 화면에서 도형을 바로 눌러 사진 한 장을 넣는다(내부 구/동 경계 없음).
NO_ZOOM_CODES = METRO_CODES | {"29"}

def project(lon, lat):
    return (lon * KX, -lat)  # y 뒤집기(화면 좌표 위->아래)

# ---------- geometry helpers ----------
def rings_of(geom):
    """feature.geometry -> [polygon...], polygon = [ring...], ring = [(x,y)...] (projected)"""
    t = geom["type"]
    polys = []
    if t == "Polygon":
        coords = [geom["coordinates"]]
    elif t == "MultiPolygon":
        coords = geom["coordinates"]
    else:
        return polys
    for poly in coords:
        rings = []
        for ring in poly:
            pts = [project(c[0], c[1]) for c in ring]
            rings.append(pts)
        polys.append(rings)
    return polys

def ring_area(pts):
    a = 0.0
    n = len(pts)
    for i in range(n):
        x1, y1 = pts[i]
        x2, y2 = pts[(i + 1) % n]
        a += x1 * y2 - x2 * y1
    return abs(a) / 2.0

def centroid(pts):
    sx = sum(p[0] for p in pts) / len(pts)
    sy = sum(p[1] for p in pts) / len(pts)
    return (sx, sy)

def poly_area(poly):
    # 외곽 링 기준
    return ring_area(poly[0]) if poly else 0.0

def bbox_of(polys):
    xs, ys = [], []
    for poly in polys:
        for ring in poly:
            for x, y in ring:
                xs.append(x); ys.append(y)
    return (min(xs), min(ys), max(xs), max(ys))

# Douglas-Peucker 단순화
def dp_simplify(pts, eps):
    if len(pts) < 3:
        return pts
    closed = pts[0] == pts[-1]
    p = pts[:-1] if closed else pts[:]
    def dp(a, b, points):
        if b <= a + 1:
            return [a]
        ax, ay = points[a]; bx, by = points[b]
        dx, dy = bx - ax, by - ay
        seglen = math.hypot(dx, dy) or 1e-12
        dmax, idx = -1, -1
        for i in range(a + 1, b):
            px, py = points[i]
            # 점-선분 거리
            t = ((px - ax) * dx + (py - ay) * dy) / (seglen * seglen)
            t = max(0.0, min(1.0, t))
            cx, cy = ax + t * dx, ay + t * dy
            d = math.hypot(px - cx, py - cy)
            if d > dmax:
                dmax, idx = d, i
        if dmax > eps:
            left = dp(a, idx, points)
            right = dp(idx, b, points)
            return left + right
        return [a]
    keep = dp(0, len(p) - 1, p) + [len(p) - 1]
    out = [p[i] for i in keep]
    if closed:
        out.append(out[0])
    return out

# Chaikin 코너 컷 스무딩 (가장자리 둥글게)
def chaikin(pts, iterations=1):
    for _ in range(iterations):
        if len(pts) < 4:
            break
        closed = pts[0] == pts[-1]
        src = pts[:-1] if closed else pts
        new = []
        n = len(src)
        rng = range(n) if closed else range(n - 1)
        for i in rng:
            p0 = src[i]
            p1 = src[(i + 1) % n]
            q = (0.75 * p0[0] + 0.25 * p1[0], 0.75 * p0[1] + 0.25 * p1[1])
            r = (0.25 * p0[0] + 0.75 * p1[0], 0.25 * p0[1] + 0.75 * p1[1])
            new.append(q); new.append(r)
        if closed:
            new.append(new[0])
        else:
            new = [src[0]] + new + [src[-1]]
        pts = new
    return pts

# ---------- normalize + path emit ----------
def split_mainland_outliers(bboxes, gap=0.15, seed_weights=None):
    """근접(bbox 간극<=gap)으로 본토 덩어리를 키우고, 멀리 떨어진 이도는 outlier로 분리.
    seed_weights: 시드 선택 기준(실제 육지 면적). 없으면 bbox 면적.
    반환: (mainland_idx_set, outlier_idx_list, mainland_bbox)."""
    n = len(bboxes)
    if n == 0:
        return set(), [], None
    if seed_weights is None:
        seed_weights = [(b[2] - b[0]) * (b[3] - b[1]) for b in bboxes]
    seed = max(range(n), key=lambda i: seed_weights[i])
    assigned = [False] * n
    assigned[seed] = True
    mb = list(bboxes[seed])
    changed = True
    while changed:
        changed = False
        for i in range(n):
            if assigned[i]:
                continue
            b = bboxes[i]
            dx = max(0.0, mb[0] - b[2], b[0] - mb[2])
            dy = max(0.0, mb[1] - b[3], b[1] - mb[3])
            if dx <= gap and dy <= gap:
                assigned[i] = True
                mb = [min(mb[0], b[0]), min(mb[1], b[1]), max(mb[2], b[2]), max(mb[3], b[3])]
                changed = True
    main = {i for i in range(n) if assigned[i]}
    outliers = [i for i in range(n) if not assigned[i]]
    return main, outliers, tuple(mb)

def make_normalizer(bbox, target_w, pad):
    minx, miny, maxx, maxy = bbox
    w = (maxx - minx) or 1e-9
    h = (maxy - miny) or 1e-9
    scale = (target_w - 2 * pad) / w
    target_h = h * scale + 2 * pad
    def nz(x, y):
        return (pad + (x - minx) * scale, pad + (y - miny) * scale)
    return nz, round(target_w, 1), round(target_h, 1)

def path_data(polys, nz, eps, smooth_iters):
    parts = []
    for poly in polys:
        for ring in poly:
            r = dp_simplify(ring, eps)
            if smooth_iters:
                r = chaikin(r, smooth_iters)
            if len(r) < 3:
                continue
            r = [nz(x, y) for x, y in r]
            d = "M%.1f,%.1f " % r[0]
            d += " ".join("L%.1f,%.1f" % (x, y) for x, y in r[1:])
            d += " Z"
            parts.append(d)
    return " ".join(parts)

# ---------- island filtering ----------
def filter_islands(polys, rel=0.02, abs_floor=0.0006, keep_far_east=True):
    """작은 이름없는 섬 제거. 육지/큰 섬 유지. 동쪽 끝(울릉/독도) 유지."""
    if not polys:
        return polys
    areas = [poly_area(p) for p in polys]
    largest = max(areas)
    largest_idx = areas.index(largest)
    kept = []
    for i, p in enumerate(polys):
        a = areas[i]
        cx, cy = centroid(p[0])
        # 투영좌표에서 lon = x/KX
        lon = cx / KX
        far_east = keep_far_east and lon > 130.5   # 울릉도(130.9)/독도(131.9)
        # 가장 큰 폴리곤(본토)은 무조건 유지 — 작은 구가 통째로 사라지지 않게
        if i == largest_idx or a >= max(rel * largest, abs_floor) or far_east:
            kept.append(p)
    return kept

# ================= build =================
def main():
    prov = json.load(open(PROV_SRC))
    muni = json.load(open(MUNI_SRC))

    # 시도별 폴리곤(투영)
    prov_feats = []
    for ft in prov["features"]:
        p = ft["properties"]
        prov_feats.append({
            "code": p["code"], "name": p["name"], "name_eng": p["name_eng"],
            "polys": rings_of(ft["geometry"]),
        })

    # ---- 줌아웃(전체 지도) ----
    # bbox는 본토+제주 기준(극동 섬이 지도를 늘리지 않게 lon<=130.2 폴리곤만 사용)
    bbox_polys = []
    filtered = {}
    for f in prov_feats:
        # 줌아웃: 작은 이름없는 섬 적극 제거(백령도/남해 잔섬), 큰 섬(강화/거제/제주)은 유지
        kept = filter_islands(f["polys"], rel=0.10, abs_floor=0.0060)
        filtered[f["code"]] = kept
        for poly in kept:
            cx, cy = centroid(poly[0])
            if (cx / KX) <= 130.2:
                bbox_polys.append(poly)
    gbbox = bbox_of(bbox_polys)
    nz, vw, vh = make_normalizer(gbbox, target_w=1000, pad=40)

    regions = []
    for f in prov_feats:
        d = path_data(filtered[f["code"]], nz, eps=0.010, smooth_iters=2)
        regions.append({
            "code": f["code"], "name": f["name"], "name_eng": f["name_eng"], "path": d,
        })
    out = {"viewBox": [vw, vh], "regions": regions}
    os.makedirs(ASSETS, exist_ok=True)
    json.dump(out, open(os.path.join(ASSETS, "provinces.json"), "w"),
              ensure_ascii=False, separators=(",", ":"))
    print(f"provinces.json  viewBox={vw}x{vh}  regions={len(regions)}")

    # ---- 줌인(시군구) : 시도별 파일 ----
    muni_feats = []
    for ft in muni["features"]:
        p = ft["properties"]
        muni_feats.append({
            "code": p["code"], "name": p["name"], "name_eng": p["name_eng"],
            "polys": rings_of(ft["geometry"]),
        })

    by_prov = {}
    for m in muni_feats:
        pc = m["code"][:2]
        by_prov.setdefault(pc, []).append(m)

    # 전국(줌아웃) 프레임에서의 각 시군 위치 인덱스 (모자이크/색농도용)
    index_items = {}
    index_counts = {}

    for pc, items in by_prov.items():
        # 광역시/세종: 줌인(시군구 분할) 안 함 → 시군 파일/인덱스 생성 생략.
        # 줌아웃에서 도형(provinces.json)을 바로 눌러 사진 한 장을 넣는다.
        if pc in NO_ZOOM_CODES:
            continue

        # 섬 정리 후 시군별 (feature, kept polys, projected bbox)
        cleaned = []
        for m in items:
            kept = filter_islands(m["polys"], rel=0.03, abs_floor=0.0008)
            if not kept:
                continue
            cleaned.append((m, kept, bbox_of(kept)))
        if not cleaned:
            continue

        # 일반 도는 실제 가장자리 유지: 약한 단순화, 스무딩 없음
        zoom_eps = 0.004

        # 본토 덩어리 vs 멀리 떨어진 이도(울릉/독도 등) 분리
        bboxes = [c[2] for c in cleaned]
        land_areas = [sum(poly_area(p) for p in c[1]) for c in cleaned]  # 실제 육지 면적
        main_set, outlier_list, mb = split_mainland_outliers(bboxes, gap=0.15, seed_weights=land_areas)
        outlier_set = set(outlier_list)

        nz_main, vw2, vh2 = make_normalizer(mb, target_w=1000, pad=30)
        inset_vb = None
        nz_inset = None
        if outlier_list:
            obb = bbox_of([p for i in outlier_list for p in cleaned[i][1]])  # outlier 폴리곤 평탄화
            nz_inset, ivw, ivh = make_normalizer(obb, target_w=300, pad=24)
            inset_vb = [ivw, ivh]

        regs = []
        index_counts[pc] = len(cleaned)
        for i, (m, kept, bx) in enumerate(cleaned):
            is_out = i in outlier_set
            nz_use = nz_inset if is_out else nz_main
            # 줌인은 실제 가장자리 유지: 약한 단순화, 스무딩 없음
            d = path_data(kept, nz_use, eps=zoom_eps, smooth_iters=0)
            regs.append({"code": m["code"], "name": m["name"],
                         "name_eng": m["name_eng"], "path": d, "inset": is_out})
            # 전국 프레임 bbox (mosaic 배치용) — 줌아웃 normalizer nz 사용
            l, t = nz(bx[0], bx[1])
            r, b = nz(bx[2], bx[3])
            # 전국 프레임 시군 경계 path — 사진 넣은 도의 '희미한 시군 경계' 표시용
            npath = path_data(kept, nz, eps=0.008, smooth_iters=0)
            index_items[m["code"]] = {
                "prov": pc,
                "b": [round(l, 1), round(t, 1), round(r, 1), round(b, 1)],
                "p": npath,
            }
        pname = next((f["name"] for f in prov_feats if f["code"] == pc), pc)
        out = {"viewBox": [vw2, vh2], "province": pname,
               "provinceCode": pc, "regions": regs}
        if inset_vb:
            out["insetViewBox"] = inset_vb
        json.dump(out, open(os.path.join(ASSETS, "sigungu", f"{pc}.json"), "w"),
                  ensure_ascii=False, separators=(",", ":"))
        ins = f" inset={len(outlier_list)}" if outlier_list else ""
        print(f"sigungu/{pc}.json ({pname}) viewBox={vw2}x{vh2} regions={len(regs)}{ins}")

    # 전국 프레임 인덱스 저장 (줌아웃 모자이크 + 도별 색농도용)
    json.dump({"viewBox": [vw, vh], "provinceCounts": index_counts, "items": index_items},
              open(os.path.join(ASSETS, "sigungu_index.json"), "w"),
              ensure_ascii=False, separators=(",", ":"))
    print(f"sigungu_index.json  items={len(index_items)}  provinces={len(index_counts)}")

if __name__ == "__main__":
    main()
