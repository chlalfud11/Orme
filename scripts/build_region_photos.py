#!/usr/bin/env python3
"""Wikimedia Commons에서 지역별 대표 카드 사진을 내려받는다."""
import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
from io import BytesIO
import json
import re
import struct
import time
import urllib.parse
import urllib.request
import urllib.error
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    Image = None


ROOT = Path(__file__).resolve().parents[1]
SEARCH = ROOT / "app/src/main/assets/search"
CATALOG = SEARCH / "regions.json"
MEDIA_SEARCH = "https://commons.wikimedia.org/w/index.php"
USER_AGENT = "Orme-region-photo-builder/2.0 (local Android project)"
MIN_REQUEST_INTERVAL = 0.2
last_request_at = 0.0
CANDIDATE_CACHE = {}
SKIP_WORDS = (
    "map",
    "flag",
    "logo",
    "seal",
    "coat of arms",
    "location map",
    "blank map",
    "military",
    "soldier",
    "army",
    "air force",
    "aircraft",
    "marine",
    "meu",
    "exercise",
    "training",
    "refuel",
    "farp",
    "support",
    "police",
    "election",
    "certificate",
    "council",
    "office",
    "station",
    "school",
    "university",
    "college",
    "hospital",
    "geology",
    "geography",
    "pull-apart",
    "pull apart",
    "earthquake",
    "disaster",
    "1928",
    "document",
    "massacre",
    "lost families",
    "circa",
    "7-eleven",
    "warehouse",
    "bus",
    "train",
    "railway",
    "station",
    "platform",
    "school",
    "university",
    "college",
    "hospital",
    "office",
    "portrait",
    "selfie",
    "animal",
    "toad",
    "frog",
    "bird",
    "fish",
    "insect",
    "fossil",
    "skeleton",
    "road sign",
    "under construction",
    "motel",
    "hotel",
    "pension",
    "guesthouse",
    "inn",
    "vw golf",
    "plaque",
    "signboard",
    "information",
    "expressway",
    "expwy",
    "interchange",
    "junction",
    "airport",
    "control tower",
    "city hall",
    "cityhall",
    "bus terminal",
    "pigeon",
    "roadsgraph",
    "express",
    "restaurant",
    "makguksu",
    "festival",
    "league",
    "football",
    "soccer",
    "orphanage",
    "children",
    "seaman",
    "navy",
    "cookies",
    "coloring",
    "shopping",
    "seafood shop",
    "street",
    "people",
    "fansign",
    "chicken",
    "turkey",
    "poultry",
    "border",
    "uss",
    "amphibious",
    "harpers ferry",
    "operation",
    "khao",
    "phan",
    "su-hyeon",
    "security unit",
    "psu",
    "coast guard",
    "snow park",
    "naval port festival",
    "maehwa festival",
    "billboard",
    "sign",
    "interior",
    "museum",
)
IMAGE_EXTENSIONS = (".jpg", ".jpeg", ".png", ".webp")
SCENIC_WORDS = (
    "temple",
    "palace",
    "fortress",
    "park",
    "lake",
    "mountain",
    "beach",
    "coast",
    "waterfall",
    "village",
    "pagoda",
    "tower",
    "sunset",
    "island",
    "arboretum",
    "river",
    "ocean",
    "bay",
    "valley",
    "garden",
    "landscape",
    "scenic",
    "trail",
    "bridge",
    "heritage",
    "shrine",
    "pavilion",
    "seaside",
    "harbor",
    "harbour",
    "cliff",
    "cave",
    "forest",
    "field",
    "tea",
    "reed",
    "wetland",
    "marsh",
    "skywalk",
    "observatory",
    "market",
    "traditional",
    "street",
    "hill",
    "sea",
)
SCENIC_WORDS_KO = (
    "사찰",
    "궁",
    "공원",
    "호수",
    "산",
    "해변",
    "해수욕장",
    "폭포",
    "섬",
    "해안",
    "다리",
    "전망대",
    "정원",
    "습지",
    "숲",
    "계곡",
    "마을",
    "성",
    "문화재",
    "풍경",
    "전경",
    "일출",
    "일몰",
    "야경",
    "바다",
    "강",
    "탑",
    "유적",
    "고분",
    "왕릉",
    "월지",
    "경포",
    "대청봉",
    "철쭉",
    "차밭",
    "갯벌",
    "항구",
    "항",
    "등대",
    "해상",
    "동굴",
    "케이블카",
)
SKIP_WORDS_KO = (
    "일제",
    "흑백",
    "군부대",
    "군사",
    "군인",
    "사람",
    "인물",
    "동물",
    "곤충",
    "비둘기",
    "문서",
    "지도",
    "학교",
    "병원",
    "사무실",
    "버스",
    "열차",
    "공사중",
    "여관",
    "호텔",
    "펜션",
    "민박",
    "간판",
    "표지판",
    "안내판",
    "실내",
    "내부",
)
LANDMARK_QUERIES = {
    "서울": ("Gyeongbokgung Palace Seoul", "Namsan Seoul Tower"),
    "부산": ("Haeundae Beach Busan", "Gamcheon Culture Village"),
    "대구": ("Apsan Park Daegu", "Suseongmot Lake Daegu"),
    "인천": ("Songdo Central Park Incheon", "Wolmido Incheon"),
    "광주": ("Mudeungsan National Park Gwangju", "Gwangju Lake Park"),
    "대전": ("Daejeon Expo Bridge", "Hanbat Arboretum"),
    "울산": ("Daewangam Park Ulsan", "Taehwagang National Garden"),
    "세종": ("Sejong Lake Park", "Sejong National Arboretum"),
    "수원": ("Hwaseong Suwon", "Janganmun Suwon"),
    "성남": ("Namhansanseong Fortress", "Tancheon Seongnam"),
    "의정부": ("Dobongsan Uijeongbu",),
    "안양": ("Anyang Art Park",),
    "부천": ("Sangdong Lake Park Bucheon",),
    "광명": ("Gwangmyeong Cave",),
    "평택": ("Pyeongtaek Lake",),
    "동두천": ("Soyosan Mountain",),
    "안산": ("Daebudo Island Ansan", "Sihwa Lake"),
    "고양": ("Ilsan Lake Park",),
    "과천": ("Seoul Grand Park Gwacheon",),
    "구리": ("Guri Hangang Park",),
    "남양주": ("Sujongsa Temple Namyangju",),
    "오산": ("Mulhyanggi Arboretum Osan",),
    "시흥": ("Gaetgol Eco Park Siheung",),
    "군포": ("Surisan Mountain Gunpo",),
    "의왕": ("Wangsong Lake Uiwang",),
    "하남": ("Misa Gyeongjeong Park Hanam",),
    "용인": ("Korean Folk Village Yongin", "Everland Yongin"),
    "파주": ("Imjingak Paju",),
    "이천": ("Icheon Ceramic Village",),
    "안성": ("Anseong Farmland",),
    "김포": ("Ara Canal Gimpo",),
    "화성": ("Jebudo Island Hwaseong",),
    "양주": ("Nari Park Yangju",),
    "포천": ("Sanjeong Lake Pocheon",),
    "여주": ("Silleuksa Temple Yeoju", "Yeongneung Royal Tombs Yeoju"),
    "연천": ("Hantangang River Yeoncheon",),
    "가평": ("Garden of Morning Calm Gapyeong",),
    "양평": ("Dumulmeori Yangpyeong",),
    "춘천": ("Soyanggang River Chuncheon",),
    "원주": ("Museum SAN Wonju",),
    "강릉": ("Gyeongpo Lake Gangneung",),
    "동해": (
        "Mureung Valley Donghae",
        "Chu-am Chotdaebawi Donghae",
        "Mukhohang Port Donghae",
    ),
    "태백": ("Taebaeksan Mountain",),
    "속초": ("Seoraksan Sokcho",),
    "삼척": ("Samcheok coast",),
    "홍천": ("Garisan Mountain Hongcheon", "Palbongsan Hongcheon", "Hongcheon river"),
    "횡성": ("Hoengseong lake", "Hoengseong Korea landscape", "Hoengseong mountain"),
    "영월": ("Seondol Yeongwol",),
    "평창": ("Daegwallyeong Pyeongchang",),
    "정선": ("Byeongbangchi Skywalk Jeongseon",),
    "철원": ("Hantangang River Cheorwon",),
    "화천": ("Hwacheon river",),
    "양구": ("Dutayeon Yanggu",),
    "인제": ("Wondae-ri Birch Forest Inje",),
    "고성": ("Goseong coast Korea",),
    "양양": ("Naksansa Temple Yangyang",),
    "충주": ("Chungju Lake",),
    "음성": ("Eumseong Seolseong Park", "Eumseong lake", "Eumseong Korea landscape"),
    "증평": ("Jeungpyeong Lake Park", "Jeungpyeong Korea landscape"),
    "제천": ("Cheongpung Lake Jecheon",),
    "청주": ("Sangdangsanseong Fortress Cheongju",),
    "보은": ("Beopjusa Temple Boeun",),
    "옥천": ("Geumgang River Okcheon",),
    "영동": ("Yeongdong Korea landscape",),
    "진천": ("Nongdari Bridge Jincheon",),
    "괴산": ("Goesan Sanmaki Old Road",),
    "단양": ("Dodamsambong Danyang",),
    "천안": ("Independence Hall Korea Cheonan",),
    "공주": ("Gongsanseong Fortress Gongju",),
    "보령": ("Daecheon Beach Boryeong",),
    "아산": ("Hyeonchungsa Shrine Asan",),
    "서산": ("Haemieupseong Fortress Seosan",),
    "논산": ("Gwanchoksa Temple Nonsan",),
    "계룡": ("Gyeryong mountain",),
    "당진": ("Sapgyo Lake Dangjin", "Waemok Village Dangjin"),
    "금산": ("Geumsan Korea mountain",),
    "부여": ("Gungnamji Pond Buyeo",),
    "서천": ("Seocheon reed field",),
    "청양": ("Cheonjangho Suspension Bridge",),
    "홍성": ("Namdanghang Harbor Hongseong",),
    "예산": ("Sudeoksa Temple Yesan",),
    "태안": ("Mongsanpo Beach Taean", "Taean coast"),
    "전주": ("Jeonju Hanok Village",),
    "군산": ("Gunsan modern cultural heritage",),
    "익산": ("Mireuksaji Temple Iksan",),
    "정읍": ("Naejangsan National Park Jeongeup",),
    "남원": ("Gwanghalluwon Garden Namwon",),
    "김제": ("Gimje rice fields",),
    "완주": ("Wanju Korea mountain",),
    "진안": ("Maisan Mountain Jinan",),
    "무주": ("Deogyusan National Park Muju",),
    "장수": ("Jangsu Korea mountain",),
    "임실": ("Imsil Cheese Village",),
    "순창": ("Sunchang Gochujang Village",),
    "고창": ("Gochang dolmen",),
    "부안": ("Chaeseokgang Cliff Buan",),
    "목포": ("Gatbawi Rock Mokpo",),
    "여수": ("Odongdo Island Yeosu",),
    "순천": ("Suncheon Bay Wetland",),
    "나주": ("Naju Yeongsanpo",),
    "광양": ("Gwangyang Maehwa Village", "Gwangyang Seomjin River"),
    "담양": ("Damyang bamboo forest",),
    "곡성": ("Seomjin Train Village Gokseong",),
    "구례": ("Hwaeomsa Temple Gurye",),
    "고흥": ("Goheung coast",),
    "보성": ("Boseong green tea fields",),
    "화순": ("Unjusa Temple Hwasun",),
    "장흥": ("Jangheung Cypress Forest",),
    "강진": ("Gangjin celadon village",),
    "해남": ("Ttangkkeut Haenam",),
    "영암": ("Wolchulsan National Park Yeongam",),
    "무안": ("Muan Lake", "Muan tidal flat", "Muan White Lotus Pond"),
    "함평": ("Hampyeong Butterfly Park",),
    "영광": ("Baeksu coastal road Yeonggwang",),
    "장성": ("Baekyangsa Temple Jangseong",),
    "완도": ("Wando Cheongsando Island",),
    "진도": ("Jindo sea coast", "Jindo Ulimsanbang Garden"),
    "신안": ("Purple Island Sinan",),
    "포항": ("Homigot Sunrise Square Pohang", "Pohang beach", "Pohang coast"),
    "경주": ("Bulguksa Temple Gyeongju", "Woljeonggyo Bridge Gyeongju"),
    "김천": ("Jikjisa Temple Gimcheon",),
    "안동": ("Hahoe Folk Village Andong",),
    "구미": ("Geumosan Mountain Gumi",),
    "영주": ("Buseoksa Temple Yeongju", "Sosu Seowon Yeongju", "Yeongju Korea landscape"),
    "영천": ("Bohyeonsa Temple Yeongcheon",),
    "상주": ("Sangju Gyeongcheon Island",),
    "문경": ("Mungyeong Saejae",),
    "경산": ("Palgongsan Mountain Gyeongsan",),
    "군위": ("Gunwi Hwasan Village",),
    "의성": ("Uiseong Ginkgo Forest",),
    "청송": ("Juwangsan National Park Cheongsong",),
    "영양": ("Yeongyang night sky park",),
    "영덕": ("Yeongdeok Sunrise Park", "Yeongdeok coast Korea", "Yeongdeok Blue Road"),
    "청도": ("Cheongdo wine tunnel",),
    "고령": ("Daegaya Historic Site Goryeong",),
    "성주": ("Seongju Korea landscape",),
    "칠곡": ("Gasan Fortress Chilgok", "Chilgok Korea landscape"),
    "예천": ("Hoeryongpo Yecheon",),
    "봉화": ("Cheongnyangsan Mountain Bonghwa",),
    "울진": ("Seongnyugul Cave Uljin",),
    "울릉": ("Ulleungdo", "Ulleungdo coast"),
    "진주": ("Jinjuseong Fortress Jinju",),
    "통영": ("Tongyeong cable car", "Tongyeong Hallyeo Waterway"),
    "사천": ("Sacheon cable car",),
    "김해": ("Daeseongdong Tombs Gimhae",),
    "밀양": ("Yeongnamnu Pavilion Miryang",),
    "거제": ("Haegeumgang Geoje", "Oedo Botania Geoje", "Geoje coast"),
    "양산": ("Tongdosa Temple Yangsan",),
    "창원": (
        "Jinhae Gyeonghwa Station cherry blossoms",
        "Yeojwacheon Romance Bridge",
        "Jinhae City Changwon",
        "Jinhae Gunhangje Festival",
    ),
    "의령": ("Uiryeong Korea landscape",),
    "함안": ("Malisan Gaya Tombs Haman",),
    "창녕": ("Upo Wetland Changnyeong",),
    "남해": ("Namhae terraced rice fields",),
    "하동": ("Hadong tea fields",),
    "산청": ("Jirisan Sancheong", "Sancheong Donguibogam Village"),
    "함양": ("Sangnim Park Hamyang",),
    "거창": ("Y-shaped suspension bridge Geochang",),
    "합천": ("Haeinsa Temple Hapcheon",),
    "제주": ("Seongsan Ilchulbong Jeju",),
    "서귀포": ("Jeju Seogwipo coast", "Jusangjeolli Cliff Seogwipo"),
}


def contains_skip_word(title):
    normalized = re.sub(r"[^a-z0-9가-힣]+", " ", title.lower())
    compact = re.sub(r"[^a-z0-9가-힣]+", "", title.lower())
    return any(
        re.search(rf"\b{re.escape(word)}\b", normalized)
        or (
            len(re.sub(r"[^a-z0-9가-힣]+", "", word)) >= 5
            and re.sub(r"[^a-z0-9가-힣]+", "", word) in compact
        )
        for word in SKIP_WORDS
    ) or any(word in title for word in SKIP_WORDS_KO)


def request_text(url, params):
    global last_request_at
    url += "?" + urllib.parse.urlencode(params)
    for attempt in range(7):
        elapsed = time.monotonic() - last_request_at
        if elapsed < MIN_REQUEST_INTERVAL:
            time.sleep(MIN_REQUEST_INTERVAL - elapsed)
        request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                last_request_at = time.monotonic()
                return response.read().decode("utf-8", errors="replace")
        except urllib.error.HTTPError as error:
            last_request_at = time.monotonic()
            if error.code != 429 or attempt == 6:
                raise
            retry_after = error.headers.get("Retry-After")
            wait_seconds = int(retry_after) if retry_after else min(30, 3 * (attempt + 1))
            print(f"rate limited; retrying in {wait_seconds}s", flush=True)
            time.sleep(wait_seconds)
        except urllib.error.URLError as error:
            if attempt == 6:
                raise
            wait_seconds = min(15, 2 * (attempt + 1))
            print(f"network error ({error.reason}); retrying in {wait_seconds}s", flush=True)
            time.sleep(wait_seconds)


def candidates(query):
    if query in CANDIDATE_CACHE:
        return CANDIDATE_CACHE[query]
    html = request_text(
        MEDIA_SEARCH,
        {
            "search": query,
            "title": "Special:MediaSearch",
            "type": "image",
            "limit": "20",
        },
    )
    result = []
    matches = list(re.finditer(
        r'"fullurl":"https://commons\.wikimedia\.org/wiki/File:([^"]+)"',
        html,
    ))
    for index, match in enumerate(matches):
        encoded_name = match.group(1)
        filename = re.sub(
            r"\\u([0-9a-fA-F]{4})",
            lambda match: chr(int(match.group(1), 16)),
            encoded_name,
        )
        title = "File:" + urllib.parse.unquote(filename)
        lower_title = title.lower()
        if lower_title.startswith("category:") or contains_skip_word(lower_title):
            continue
        if not lower_title.endswith(IMAGE_EXTENSIONS):
            continue
        filename = title.removeprefix("File:")
        end = matches[index + 1].start() if index + 1 < len(matches) else len(html)
        metadata = html[match.end():end]
        thumb_match = re.search(r'"thumburl":"([^"]+)"', metadata)
        responsive_match = re.search(
            r'"responsiveUrls":\{"2":"([^"]+)"',
            metadata,
        )
        thumb_url = (
            responsive_match.group(1)
            if responsive_match
            else thumb_match.group(1)
            if thumb_match
            else (
                "https://commons.wikimedia.org/wiki/Special:FilePath/"
                + urllib.parse.quote(filename, safe="")
                + "?width=800"
            )
        )
        thumb_url = re.sub(
            r"\\u([0-9a-fA-F]{4})",
            lambda value: chr(int(value.group(1), 16)),
            thumb_url,
        )
        result.append({
            "key": title,
            "title": title,
            "thumb_url": thumb_url,
            "page_url": "https://commons.wikimedia.org/wiki/"
                + urllib.parse.quote(title.replace(" ", "_")),
        })
    CANDIDATE_CACHE[query] = result
    return result


def korean_base(name):
    for suffix in ("특별자치시", "광역시", "특별시"):
        if name.endswith(suffix):
            return name[:-len(suffix)]
    if name.endswith("시"):
        return name[:-1]
    if "시" in name:
        return name.split("시", 1)[0]
    if name.endswith(("군", "구")):
        return name[:-1]
    return name


def english_base(name):
    if name.lower() == "daegu":
        return name
    if re.search(r"(?i)[- ](?:si|gun|gu)$", name):
        return re.split(r"(?i)[- ](?:si|gun|gu)$", name, maxsplit=1)[0]
    lower = name.lower()
    for suffix in ("gwangyeoksi", "teukbyeoljasi", "teukbyeolsi"):
        if lower.endswith(suffix):
            return name[:-len(suffix)]
    if lower.endswith("gun"):
        return name[:-3]
    if lower.endswith("gu"):
        stem = name[:-2]
        index = stem.lower().find("si")
        return stem[:index] if index > 2 else stem
    if lower.endswith("si"):
        return name[:-2]
    return name


def query_terms(entry):
    korean = korean_base(entry["name"])
    english = english_base(entry["name_eng"])
    result = []
    for query in LANDMARK_QUERIES.get(korean, ()):
        if query and query not in result:
            result.append(query)
        words = query.split()
        if len(words) > 2:
            short_query = " ".join(words[:2])
            if short_query not in result:
                result.append(short_query)
    for query in (korean, english):
        if query and query not in result:
            result.append(query)
    return result


def candidate_score(candidate, entry):
    title = candidate["title"].lower()
    score = 0
    korean = korean_base(entry["name"])
    english = english_base(entry["name_eng"]).lower()
    if korean and korean.lower() in title:
        score += 30
    if english and len(english) >= 4 and english in title:
        score += 24
    for query in LANDMARK_QUERIES.get(korean, ()):
        for word in query.lower().split():
            if len(word) >= 4 and word in title:
                score += 28
    for word in SCENIC_WORDS:
        if word in title:
            score += 5
    for word in SCENIC_WORDS_KO:
        if word in title:
            score += 6
    if contains_skip_word(title):
        score -= 30
    if re.search(r"(?i)(dsc|img[_ -]|\b20\d{2}\b)", title):
        score -= 4
    if len(candidate["title"]) < 90:
        score += 2
    return score


GENERIC_LANDMARK_WORDS = {
    "beach",
    "bridge",
    "cable",
    "car",
    "coast",
    "forest",
    "garden",
    "hill",
    "island",
    "korea",
    "lake",
    "landscape",
    "market",
    "mountain",
    "museum",
    "national",
    "park",
    "river",
    "road",
    "sea",
    "skywalk",
    "south",
    "temple",
    "tower",
    "village",
    "windy",
}


def candidate_is_local(candidate, entry):
    title = candidate["title"].lower()
    korean = korean_base(entry["name"]).lower()
    english = english_base(entry["name_eng"]).lower()
    if (korean and korean in title) or (len(english) >= 4 and english in title):
        return True
    for query in LANDMARK_QUERIES.get(korean_base(entry["name"]), ()):
        words = [
            word.lower()
            for word in re.findall(r"[a-zA-Z]+", query)
            if len(word) >= 4
        ]
        if words and words[0] not in GENERIC_LANDMARK_WORDS and words[0] in title:
            return True
        matched = [word for word in words if word in title]
        if len(matched) >= 2 and any(
            word not in GENERIC_LANDMARK_WORDS for word in matched
        ):
            return True
    return False


def image_dimensions(data):
    if data.startswith(b"\x89PNG\r\n\x1a\n") and len(data) >= 24:
        return struct.unpack(">II", data[16:24])
    if data[8:12] == b"WEBP":
        if data[12:16] == b"VP8X" and len(data) >= 30:
            width = 1 + int.from_bytes(data[24:27], "little")
            height = 1 + int.from_bytes(data[27:30], "little")
            return width, height
        return 0, 0
    if not data.startswith(b"\xff\xd8\xff"):
        return 0, 0
    offset = 2
    while offset + 9 < len(data):
        if data[offset] != 0xFF:
            offset += 1
            continue
        while offset < len(data) and data[offset] == 0xFF:
            offset += 1
        if offset >= len(data):
            break
        marker = data[offset]
        offset += 1
        if marker in (0xD8, 0xD9):
            continue
        if offset + 2 > len(data):
            break
        length = int.from_bytes(data[offset:offset + 2], "big")
        if marker in {
            0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7,
            0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF,
        } and offset + 7 <= len(data):
            height = int.from_bytes(data[offset + 3:offset + 5], "big")
            width = int.from_bytes(data[offset + 5:offset + 7], "big")
            return width, height
        offset += max(length, 2)
    return 0, 0


def has_color(data):
    if Image is None:
        return True
    try:
        with Image.open(BytesIO(data)) as image:
            pixels = list(image.convert("RGB").resize((32, 32)).getdata())
        colorful = sum(1 for pixel in pixels if max(pixel) - min(pixel) >= 16)
        average_spread = sum(max(pixel) - min(pixel) for pixel in pixels) / len(pixels)
        return colorful >= len(pixels) * 0.30 and average_spread >= 22
    except Exception:
        return False


def choose_photo(entry, used_urls, used_hashes):
    for query in query_terms(entry):
        ordered = sorted(
            [
                candidate for candidate in candidates(query)
                if candidate["thumb_url"] not in used_urls
                and not contains_skip_word(candidate["title"].lower())
                and candidate_is_local(candidate, entry)
            ],
            key=lambda candidate: candidate_score(candidate, entry),
            reverse=True,
        )
        for offset in range(0, len(ordered), 6):
            batch = ordered[offset:offset + 6]
            with ThreadPoolExecutor(max_workers=4) as executor:
                downloads = list(executor.map(download_candidate, batch))
            for photo, data in zip(batch, downloads):
                if data is None:
                    used_urls.add(photo["thumb_url"])
                    continue
                if image_hash(data) in used_hashes:
                    used_urls.add(photo["thumb_url"])
                    continue
                width, height = image_dimensions(data)
                if min(width, height) < 320 or max(width, height) > min(width, height) * 3:
                    used_urls.add(photo["thumb_url"])
                    continue
                if not has_color(data):
                    used_urls.add(photo["thumb_url"])
                    continue
                return photo, data
    return None, None


def download_candidate(photo):
    try:
        return download(photo["thumb_url"])
    except (RuntimeError, urllib.error.HTTPError, urllib.error.URLError):
        return None


def download(url):
    if url.startswith("//"):
        url = "https:" + url
    for attempt in range(3):
        request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
        try:
            with urllib.request.urlopen(request, timeout=60) as response:
                data = response.read()
            break
        except urllib.error.URLError:
            if attempt == 2:
                raise
            time.sleep(2 * (attempt + 1))
    if len(data) < 1024:
        raise RuntimeError("downloaded image is unexpectedly small")
    is_jpeg = data.startswith(b"\xff\xd8\xff")
    is_png = data.startswith(b"\x89PNG\r\n\x1a\n")
    is_webp = data[8:12] == b"WEBP"
    if not (is_jpeg or is_png or is_webp):
        raise RuntimeError("downloaded response is not an image")
    return data


def image_hash(data):
    return hashlib.sha256(data).hexdigest()


def select_photo(entry):
    return choose_photo(entry, set(), set())


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--only", nargs="*", help="download only these region codes")
    parser.add_argument("--keep-existing", action="store_true")
    parser.add_argument("--replace-existing", action="store_true")
    args = parser.parse_args()

    entries = json.loads(CATALOG.read_text(encoding="utf-8"))
    selected = (
        [entry for entry in entries if entry["code"] in args.only]
        if args.only
        else entries
    )
    SEARCH.mkdir(parents=True, exist_ok=True)
    if not args.only and not args.keep_existing:
        for path in SEARCH.glob("*.jpg"):
            path.unlink()

    results = {}
    with ThreadPoolExecutor(max_workers=3) as executor:
        for entry, result in zip(selected, executor.map(select_photo, selected)):
            results[entry["code"]] = result
    resolved = {}
    used_urls = set()
    selected_images = {entry["image"] for entry in selected}
    used_hashes = {
        image_hash(path.read_bytes())
        for path in SEARCH.glob("*.jpg")
        if path.name not in selected_images
    }
    base_photos = {}
    for entry in selected:
        photo, data = results[entry["code"]]
        if photo is None:
            photo, data = choose_photo(entry, used_urls, used_hashes)
        if photo is not None and image_hash(data) in used_hashes:
            photo, data = choose_photo(entry, used_urls, used_hashes)
        if photo is None:
            fallback = base_photos.get(korean_base(entry["name"]))
            if fallback is None:
                raise RuntimeError(
                    f"no local Wikimedia photo found for {entry['code']} {entry['name']}"
                )
            # ponytail: same-city subdivisions reuse a confirmed landmark when
            # Commons has no second suitable photo; add a new image only when it exists.
            photo, data = fallback
        resolved[entry["code"]] = (photo, data)
        used_urls.add(photo["thumb_url"])
        used_hashes.add(image_hash(data))
        base_photos.setdefault(korean_base(entry["name"]), (photo, data))
    sources = [
        "Region-card photos downloaded from Wikimedia Commons.",
        "Each line below records the source page for the matching local asset.",
        "",
    ]
    for index, entry in enumerate(selected, 1):
        output = SEARCH / entry["image"]
        if output.exists() and args.keep_existing and not args.replace_existing:
            sources.append(
                f"{entry['code']} {entry['name']} | existing local asset | "
                f"{entry['image']}"
            )
            print(f"[{index}/{len(selected)}] keep {entry['code']}")
            continue
        photo, data = resolved[entry["code"]]
        output.write_bytes(data)
        sources.append(
            f"{entry['code']} {entry['name']} | {photo['title']} | "
            f"{photo['page_url']}"
        )
        print(f"[{index}/{len(selected)}] {entry['code']} {entry['name']}")

    manifest = SEARCH / "SOURCES.txt"
    if not args.only:
        manifest.write_text("\n".join(sources) + "\n", encoding="utf-8")
    elif manifest.exists():
        updates = {
            line.split(" ", 1)[0]: line
            for line in sources[3:]
        }
        merged = []
        for line in manifest.read_text(encoding="utf-8").splitlines():
            code = line.split(" ", 1)[0]
            merged.append(updates.get(code, line))
        manifest.write_text("\n".join(merged) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
