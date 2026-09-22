#!/usr/bin/env python3
"""재생성된 지도 산출물에 울릉도·독도 경로가 남아 있는지 확인한다."""
import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MAP = ROOT / "app/src/main/assets/map"


def load(name):
    return json.loads((MAP / name).read_text(encoding="utf-8"))


provinces = load("provinces.json")
assert len(provinces["regions"]) == 17, "시도 지도는 17개여야 합니다."
province_gyeongbuk = next(
    region for region in provinces["regions"] if region["code"] == "37"
)
province_points = [
    (float(x), float(y))
    for x, y in re.findall(r"([0-9.-]+),([0-9.-]+)", province_gyeongbuk["path"])
]
assert max(x for x, _ in province_points) > provinces["viewBox"][0], (
    "동해 섬은 초기 지도 바깥에서 수평 이동으로 접근 가능해야 합니다."
)
assert max(y for _, y in province_points) <= provinces["viewBox"][1]

gyeongbuk = load("sigungu/37.json")
ulleung = next(
    region for region in gyeongbuk["regions"] if region["code"] == "37430"
)
assert ulleung["name"] == "울릉군"
assert ulleung["inset"], "울릉군은 분리된 섬 인셋이어야 합니다."
assert gyeongbuk.get("insetViewBox"), "경상북도 인셋 뷰박스가 없습니다."
assert ulleung["path"].count("M") >= 5, "울릉도·독도 멀티폴리곤이 줄었습니다."
assert len(gyeongbuk.get("insetFrame", [])) == 4, (
    "울릉도·독도를 본토의 실제 동쪽 위치에 그릴 인셋 프레임이 없습니다."
)
assert gyeongbuk["insetFrame"][0] > gyeongbuk["viewBox"][0], (
    "울릉도·독도 인셋 프레임은 본토 오른쪽에 있어야 합니다."
)

index = load("sigungu_index.json")
assert "37430" in index["items"], "전국 시군구 인덱스에 울릉군이 없습니다."

print(
    "verified: 17 provinces, Ulleung-gun inset, "
    f"{ulleung['path'].count('M')} island paths, nationwide index entry"
)
