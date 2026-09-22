#!/usr/bin/env python3
"""전국 지역 카탈로그와 카드 이미지의 일관성을 확인한다."""
import hashlib
import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SEARCH = ROOT / "app/src/main/assets/search"
entries = json.loads((SEARCH / "regions.json").read_text(encoding="utf-8"))

assert len(entries) == 183, f"지역 카드 수: {len(entries)}"
codes = [entry["code"] for entry in entries]
assert len(set(codes)) == len(codes), "지역 코드가 중복됩니다."
assert all(
    entry["name"]
    and entry["name_eng"]
    and entry["image"]
    and -90 <= entry["latitude"] <= 90
    and -180 <= entry["longitude"] <= 180
    for entry in entries
), "지역 메타데이터가 비어 있거나 유효하지 않습니다."
assert all((SEARCH / entry["image"]).is_file() for entry in entries), (
    "카탈로그가 참조하는 사진이 없습니다."
)
asset_names = {path.name for path in SEARCH.glob("region_*.jpg")}
assert asset_names == {entry["image"] for entry in entries}, (
    "지역 카드 자산과 카탈로그가 일치하지 않습니다."
)
assert all(entry["image"].startswith("region_") for entry in entries), (
    "지역별 사진 파일명이 아닙니다."
)
hashes = {
    hashlib.sha256((SEARCH / entry["image"]).read_bytes()).hexdigest()
    for entry in entries
}
assert len(hashes) == len(entries), "지역별 사진 파일 내용이 중복됩니다."

sources = (SEARCH / "SOURCES.txt").read_text(encoding="utf-8").splitlines()
source_entries = [
    line for line in sources
    if re.match(r"^\d+\s", line)
]
assert len(source_entries) == len(entries), "지역별 사진 출처 기록이 누락되었습니다."
assert all(
    "https://commons.wikimedia.org/wiki/" in line
    and "| Flickr:" not in line
    for line in source_entries
), "지역별 사진은 Wikimedia Commons 출처만 사용해야 합니다."

print(
    f"verified: {len(entries)} regions, "
    f"{len(hashes)} unique bundled photos"
)
