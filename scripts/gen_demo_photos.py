#!/usr/bin/env python3
"""데모용: 여러 시군을 '시군 모양대로 마스킹한' 사진 PNG로 생성 (앱 filesDir에 주입)."""
import json, os, colorsys
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "app/src/main/assets/map")
OUT = "/tmp/demo_photos"
os.makedirs(OUT, exist_ok=True)

def parse_path(d):
    subs, cur = [], []
    d = d.replace("Z", " Z ").replace("M", " M ").replace("L", " L ")
    for p in d.split():
        if p in ("M", "L"):
            if p == "M" and cur:
                subs.append(cur); cur = []
        elif p == "Z":
            if cur: subs.append(cur); cur = []
        else:
            x, y = p.split(","); cur.append((float(x), float(y)))
    if cur: subs.append(cur)
    return subs

def gen(code, path_d, hue):
    subs = parse_path(path_d)
    pts = [p for s in subs for p in s]
    xs = [p[0] for p in pts]; ys = [p[1] for p in pts]
    minx, miny, maxx, maxy = min(xs), min(ys), max(xs), max(ys)
    scale = 400.0 / max(maxx - minx, maxy - miny)
    w = max(1, int((maxx - minx) * scale)); h = max(1, int((maxy - miny) * scale))
    # 사진(그라디언트) + 마스크
    r, g, b = [int(c * 255) for c in colorsys.hsv_to_rgb(hue, 0.55, 0.9)]
    r2, g2, b2 = [int(c * 255) for c in colorsys.hsv_to_rgb((hue + 0.5) % 1, 0.6, 0.7)]
    photo = Image.new("RGB", (w, h))
    for yy in range(h):
        t = yy / max(1, h - 1)
        row = (int(r + (r2 - r) * t), int(g + (g2 - g) * t), int(b + (b2 - b) * t))
        for xx in range(0, w):
            photo.putpixel((xx, yy), row)
    mask = Image.new("L", (w, h), 0)
    md = ImageDraw.Draw(mask)
    for s in subs:
        if len(s) >= 3:
            poly = [((x - minx) * scale, (y - miny) * scale) for x, y in s]
            md.polygon(poly, fill=255)
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    out.paste(photo, (0, 0), mask)
    out.save(os.path.join(OUT, f"{code}.png"))
    return code

def main():
    codes = []
    files = sorted(os.listdir(os.path.join(ASSETS, "sigungu")))
    for i, fn in enumerate(files):
        data = json.load(open(os.path.join(ASSETS, "sigungu", fn)))
        regs = data["regions"]
        # 도마다 앞쪽 2개 시군 채움
        for j, r in enumerate(regs[:2]):
            hue = ((i * 2 + j) * 0.13) % 1.0
            codes.append(gen(r["code"], r["path"], hue))
    print(f"generated {len(codes)} demo photos -> {OUT}")
    print(" ".join(codes))

if __name__ == "__main__":
    main()
