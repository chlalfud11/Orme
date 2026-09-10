#!/usr/bin/env python3
"""생성된 path JSON을 PIL로 렌더링해서 PNG와 비교 (튜닝용, 앱 빌드 불필요)."""
import json, sys, os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "app/src/main/assets/map")

def parse_path(d):
    """M/L/Z 절대좌표만 지원 -> subpath 리스트([[(x,y)...], ...])"""
    subs, cur = [], []
    i, n = 0, len(d)
    tok = ""
    # 간단 파서
    d = d.replace("Z", " Z ").replace("M", " M ").replace("L", " L ")
    parts = d.split()
    mode = None
    for p in parts:
        if p in ("M", "L"):
            mode = p
            if p == "M":
                if cur: subs.append(cur)
                cur = []
        elif p == "Z":
            if cur: subs.append(cur); cur = []
            mode = None
        else:
            x, y = p.split(",")
            cur.append((float(x), float(y)))
    if cur: subs.append(cur)
    return subs

def render(path_json, out_png, fill=(150,166,140), edge=(110,125,100), bg=(242,237,213)):
    data = json.load(open(path_json))
    vw, vh = data["viewBox"]
    scale = 500.0 / vw
    W, H = int(vw*scale), int(vh*scale)
    img = Image.new("RGB", (W, H), bg)
    dr = ImageDraw.Draw(img)
    for reg in data["regions"]:
        for sub in parse_path(reg["path"]):
            pts = [(x*scale, y*scale) for x, y in sub]
            if len(pts) >= 3:
                dr.polygon(pts, fill=fill, outline=edge)
    img.save(out_png)
    print(f"{out_png}  {W}x{H}  regions={len(data['regions'])}")

if __name__ == "__main__":
    render(os.path.join(ASSETS, "provinces.json"),
           "/tmp/preview_provinces.png")
    render(os.path.join(ASSETS, "sigungu/32.json"),
           "/tmp/preview_gangwon.png")
