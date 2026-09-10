<!-- STATUS: final — research converged and lead-reviewed 2026-08-11 -->

# Orme 여행 기록 앱 차별화 조사

> 조사 기준일: 2026-08-10~11 · 대상: 한국 여행 기록 앱·웹사이트와 Orme
>
> 실행 요약: 팀 멤버 8명 + 보조 레인 18개 시도(Provider 오류 후 lead 직접
> 수행) · 확장 파동 2회 · excursion 0회 · 출처 50개(웹 22개 호스트) · 검증
> 3건 · debate 4회 · 경과 38분

## 1. 결론 요약

### 한 문장 결론

Orme는 **“지도를 사진으로 채우는 앱”**이 아니라, **“한국의 한 지역을
완성하고 그 지역의 사진을 손으로 만든 한 장의 기억 페이지로 남기는 앱”**으로
포지셔닝하는 편이 낫다.

### 조사에서 확인한 것

1. 한국 시·군·구를 칠하거나 사진을 배치하는 서비스가 이미 있다. [추억지도
   [S-023]](https://www.memory-map.org/), [마이코리아맵 [S-033]](https://play.google.com/store/apps/details?id=com.my.koreamap&hl=en-US),
   [여정 [S-034]](https://play.google.com/store/apps/details?id=com.travelmap.app&hl=en-US)이
   지역 지도, 방문 상태, 사진·메모, 통계 또는 추천을 제공한다.
2. 사진을 지역 모양에 맞춰 자르는 발상도 독점적이지 않다. [Travel Memory Map
   [S-046]](https://apps.apple.com/us/app/travel-memory-map/id6478291625)은
   일본 도도부현 모양으로 사진을 잘라 붙이고, 일부 시·정·촌 단위까지 확장한다고
   설명한다.
3. EXIF 자동 정리, GPS 경로, 지도 핀, 통계, 배지, 비공개 보관, 스크랩북, AI
   회고도 각각 이미 여러 제품이 차지한 기능 영역이다. [Flyve
   [S-024]](https://flyve.kr/), [Polarsteps
   [S-025]](https://www.polarsteps.com/travel-tracker),
   [Journily [S-035]](https://journiapp.io/), [TripMemo
   [S-049]](https://tripmemo.app/travel-photo-journal-app)가 대표적이다.
4. 따라서 차별화 포인트는 단일 기능이 아니라 **지역 진행도 → 사진 선택 →
   지역 모양 사진 → 편집 가능한 다이어리 페이지 → 다시 지도에서 확인**으로
   이어지는 완성 루프여야 한다. 이 문장은 시장 독점 사실이 아니라, 현재 Orme
   코드와 조사 결과에서 도출한 **검증할 제품 가설**이다.

### 가장 먼저 만들 기능

| 우선순위 | 기능 | 이유 |
| --- | --- | --- |
| P0 | 지역 기록 상태를 `방문 예정 / 사진 기록 / 다이어리 완성`으로 분리 | 사진과 다이어리가 분리되어 있어 연결 가치가 크다. |
| P0 | 지역 상세의 “사진 채우기 → 다이어리 만들기” 완성 카드 | 기존 지도·사진 크롭·에디터를 재사용한다. |
| P1 | 지역별 기억 페이지 템플릿과 짧은 회고 프롬프트 | 빈 캔버스 부담을 낮추고 결과물을 남긴다. |
| P1 | EXIF 날짜/GPS 가져오기 + 사용자가 확인하는 지역 매칭 | 차별화보다 기록 마찰 감소를 위한 편의 기능이다. |
| P2 | 미방문 지역의 TourAPI 추천과 위시리스트 | 한국 어필은 크지만 네트워크·키 처리가 필요하다. |
| P2 | 지역별 공유 카드/이미지 내보내기 | 서버 없이 결과물을 공유할 수 있다. |

반대로 처음부터 백엔드·실시간 GPS·팔로우·리더보드·LLM 일기 생성을 넣는 것은
차별화보다 범위와 운영 부담을 키운다. 인증·동기화는 서비스 완성도에는 중요하지만
Orme만의 기능이라고 말할 수는 없다.

## 2. Orme 현재 제품 이해

### 관찰된 현재 상태

아래는 기획서와 소스 코드에서 직접 확인한 사실이다.

| 영역 | 현재 확인 내용 | 근거 |
| --- | --- | --- |
| 지도 | 전국 시·도에서 도별 시·군으로 내려가며, 지역을 누르면 사진을 넣을 수 있다. 지역 모양으로 사진을 크롭하고 수동 위치를 조정한다. | [`handoff.md`](../project/handoff.md), [`MapModels.kt`](../../app/src/main/java/com/orme/app/ui/map/MapModels.kt), [`PhotoStore.kt`](../../app/src/main/java/com/orme/app/ui/map/PhotoStore.kt) |
| 성취감 | 전국 모자이크와 도별 완성도 색으로 “채워지는 지도”를 보여준다. | [`project-ideas.md`](../project/project-ideas.md), [`MapScreen.kt`](../../app/src/main/java/com/orme/app/ui/map/MapScreen.kt) |
| 다이어리 | 표지·내지 선택, 펜·텍스트·스티커·사진·실행 취소/재실행이 있는 편집기가 있다. | [`handoff.md`](../project/handoff.md), [`Diary.kt`](../../app/src/main/java/com/orme/app/ui/diary/Diary.kt), [`DecorateScreen.kt`](../../app/src/main/java/com/orme/app/ui/diary/DecorateScreen.kt) |
| 저장 | 지도 사진은 `filesDir/map_photos`, 다이어리는 별도 로컬 저장 흐름을 사용한다. | [`PhotoStore.kt`](../../app/src/main/java/com/orme/app/ui/map/PhotoStore.kt), [`RegionDiaryFlow.kt`](../../app/src/main/java/com/orme/app/ui/diary/RegionDiaryFlow.kt) |
| 인증 | 로그인·회원가입 화면은 있으나 실제 인증은 없고, 아무 값으로 다음 화면에 들어갈 수 있다. | [`handoff.md`](../project/handoff.md), [`NavGraph.kt`](../../app/src/main/java/com/orme/app/navigation/NavGraph.kt) |
| 외부 기능 | 현재 의존성에는 네트워크·지도 SDK·EXIF 라이브러리가 없고, Manifest에도 위치 권한이 없다. 갤러리는 `GetContent`로 연다. | [`app/build.gradle.kts`](../../app/build.gradle.kts), [`AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml), [`DecorateScreen.kt`](../../app/src/main/java/com/orme/app/ui/diary/DecorateScreen.kt) |

### 핵심 문제

현재 구조는 기능이 없는 것이 아니라 **두 개의 좋은 기능이 연결되지 않은
상태**다.

- 지도 사진 저장의 기준은 지역 코드와 파일 존재 여부다.
- 다이어리의 기준은 별도 `DiaryRecord`와 꾸미기 요소다.
- 따라서 지도에는 사진이 있지만 “이 지역의 여행 기억을 완성했다”는 상태가
  데이터 모델에 명시되어 있지 않다.
- 이 연결을 먼저 만들면 새 서버나 AI 없이도 현재 앱의 깊이가 생긴다.

## 3. 경쟁 서비스 비교

### 3.1 한국 지도·지역 기록

| 서비스 | 관찰된 기능 | Orme와 겹치는 부분 | Orme가 피해야 할 해석 |
| --- | --- | --- | --- |
| [추억지도 [S-023]](https://www.memory-map.org/) | 한국 시·군·구 지도에 사진을 배치하고 검색·확대·이동한다. | 한국 행정구역 단위 + 사진 지도 | “시·군·구 사진 지도” 자체를 독점 기능으로 주장하지 않는다. |
| [마이코리아맵 [S-033]](https://play.google.com/store/apps/details?id=com.my.koreamap&hl=en-US) | 지역 색칠, 전국·지역 통계, 국가 완주 도전, 업적, 지도 저장·공유, 계정 동기화를 설명한다. | 한국 지역 정복·통계·업적 | 정복률과 배지만 추가해 차별화했다고 말하지 않는다. |
| [여정 [S-034]](https://play.google.com/store/apps/details?id=com.travelmap.app&hl=en-US) | 한국·서울·일본·세계 지도, 방문/가고 싶은 곳, 사진·메모·태그·날짜, 캘린더·통계·추천·동기화 | 방문 상태, 기록, 추천, 통계가 이미 한 흐름에 있다. | 위시리스트·추천·캘린더만으로는 부족하다. |
| [포토로그 [S-022]](https://www.photolog.kr/) | 여행자가 공개한 여행기를 보고 탐색하는 구조를 내세운다. | 사진을 여행 기록으로 남김 | 공개 피드와 팔로우를 넣는 것이 자동으로 Orme의 답은 아니다. |
| [Flyve [S-024]](https://flyve.kr/) | 사진의 위치·시간을 읽어 국가·도시 앨범을 자동 구성하고 배지·도시 랭킹·위시리스트·여행자 연결을 제공한다고 설명한다. | 사진 기반 지도·성취·다음 여행 | 자동화·랭킹·소셜을 따라가면 이미 강한 경쟁 영역으로 들어간다. |
| [Travel Memory Map [S-046]](https://apps.apple.com/us/app/travel-memory-map/id6478291625) | 사진을 퍼즐처럼 도도부현 모양으로 잘라 붙이고, 일부 지역은 시·정·촌 단위로 제공한다고 설명한다. | 사진을 행정구역 모양에 맞추는 시각적 발상 | Orme의 사진 모양 크롭을 세계 최초처럼 표현하지 않는다. |

### 3.2 자동 추적·지도·일기

| 서비스 | 관찰된 기능 | Orme와의 관계 | 판단 |
| --- | --- | --- | --- |
| [Polarsteps [S-025]](https://www.polarsteps.com/travel-tracker) | 이동 경로 추적, 디지털 여행 일기, 실시간 공유, 통계, 여러 기기에서 보기 | 여행 중 자동 기록 중심 | Orme MVP는 경로 추적이 아니라 여행 후 지역 기억 완성에 집중한다. |
| [FindPenguins [S-026]](https://findpenguins.com/why-choose-findpenguins) | 하루 여러 게시물, 사진·영상, 플라이오버, 경로 편집·공백 보정, 지연 공개, 세밀한 공개 설정, 여행 책 | 경로·책·공유를 넓게 제공 | 배터리·권한·서버·공유 운영이 필요한 영역은 후순위다. |
| [been [S-027]](https://been.app/) / [map maker [S-028]](https://been.app/map-maker) | 방문 국가·지역, 여권·통계, 오프라인·동기화·위시리스트, 무계정 지도 생성·이미지 공유 | 방문 지도와 공유 카드가 겹친다. | 통계·지도 이미지 내보내기만으로는 차별화되지 않는다. |
| [MapMemories [S-048]](https://play.google.com/store/apps/details?id=com.mapmemories.app&hl=en) | EXIF GPS 핀, 수동 위치 보정, 여행 묶음, 경로 재생, 릴스, 통계, 개인 Google Drive 백업을 설명한다. | EXIF와 개인 저장은 이미 존재한다. | “로컬 저장”만을 차별점으로 내세우지 않는다. |

### 3.3 다이어리·스크랩북·계획

| 서비스 | 관찰된 기능 | Orme와의 관계 | 판단 |
| --- | --- | --- | --- |
| [Day One [S-029]](https://dayoneapp.com/features/) | 서식 있는 글, 사진·영상·오디오·그림, 지도·캘린더·사진 보기, 검색·필터, 메타데이터, 보안·내보내기 | 다이어리 깊이와 회고 기능이 강하다. | 기능 수 경쟁 대신 한국 지역 완성 맥락을 붙인다. |
| [D·LOG [S-032]](https://intro.dlog.me/) | 사진의 시간·위치·날씨를 이용해 순간을 하루와 타임라인으로 묶는 흐름을 내세운다. | 자동 기록 편의가 겹친다. | EXIF는 차별점이 아니라 입력 마찰을 낮추는 보조 기능이다. |
| [Journily [S-035]](https://journiapp.io/) | 비공개 여행 스크랩북, 지도, AI 회고, 오디오, PDF, 타임캡슐을 제공한다고 설명한다. | 스크랩북·비공개·AI·지도 조합이 겹친다. | “따뜻한 스크랩북”이라는 감성만 복제하지 않는다. |
| [TripMemo [S-049]](https://tripmemo.app/travel-photo-journal-app) | 사진 중심 일일 페이지, 장소 핀, 비공개 기본값, 오프라인 작성, 책처럼 공유하는 결과물 | 사진→페이지 전환이 겹친다. | Orme는 페이지 자체보다 **지역 완료와 연결된 페이지**를 실험한다. |
| [Wanderlog [S-031]](https://wanderlog.com/plan-a-trip) | 일정, 지도·경로, 추천, 예산, 예약, 협업, 오프라인, 패킹 목록 | 여행 전 계획 영역의 강자다. | 범용 일정·예약 앱으로 확장하지 않는다. |

## 4. 차별화 공백과 제품 가설

### 4.1 이미 점유된 기능

다음은 구현할 수는 있지만, 단독으로는 차별화라고 부르기 어렵다.

- 지도에 방문 지역을 색칠하기
- 지역별 사진·메모·태그·날짜 기록
- 사진 EXIF에서 위치·시간 읽기
- 통계·정복률·배지·업적
- 비공개·오프라인 저장
- AI 캡션·여행 요약
- 경로 자동 추적·플라이오버
- 위시리스트·관광지 추천
- 이미지·PDF·책 내보내기
- 팔로우·댓글·랭킹

이 판단은 “기능이 쓸모없다”는 뜻이 아니다. 위 기능들은 **핵심 루프를
강화하는 보조 기능**으로 배치해야 하며, 제품 소개의 첫 문장이 되어서는 안 된다는
뜻이다. 위 기능들의 선행 사례는 [Flyve](https://flyve.kr/),
[FindPenguins](https://findpenguins.com/why-choose-findpenguins),
[Day One](https://dayoneapp.com/features/), [Journily](https://journiapp.io/),
[Yeojeong](https://play.google.com/store/apps/details?id=com.travelmap.app&hl=en-US)에
확인된다.

### 4.2 Orme에 적합한 차별화 가설

> **가설:** 사용자는 지역을 방문했다는 체크만 하는 것이 아니라, 그 지역을
> 사진 한 조각과 다이어리 한 장으로 “완성”했을 때 더 강한 소유감과 회고 가치를
> 느낀다.

이 가설을 제품 흐름으로 바꾸면 다음과 같다.

```text
미방문/가고 싶은 지역
        ↓
지역을 방문하고 사진 선택
        ↓
사진을 지역 모양으로 채움
        ↓
짧은 회고 프롬프트와 다이어리 페이지 완성
        ↓
지도 상태가 “사진 기록”에서 “기억 완성”으로 변경
        ↓
나중에 지역 카드·지도·페이지로 다시 회고
```

여기서 중요한 것은 “이 흐름을 어떤 경쟁자도 제공하지 않는다”가 아니다.
이번 조사만으로 부재를 증명할 수 없기 때문이다. 정확한 표현은 **Orme의 현재
구현 자산을 가장 적게 버리고, 여러 경쟁 제품의 기능을 단순 복제하지 않으면서
검증할 수 있는 조합**이다.

### 4.3 차별화 문장

발표나 README에는 다음처럼 쓰는 것이 안전하다.

> Orme는 여행 경로를 자동 추적하는 앱이 아니라, 한국의 지역을 하나씩 채우고
> 그 지역의 사진과 이야기를 손으로 만든 기억 페이지로 완성해 가는 여행
> 기록 앱이다.

“국내 최초”, “유일한”, “경쟁자가 없는” 표현은 조사 근거가 없으므로 사용하지
않는다.

## 5. 추천 기능 우선순위

점수는 시장 점유율이 아니라 **Orme 적합성(5)·차별화 기여(5)·현재 코드
재사용성(5)·구현 부담(5)**을 고려한 상대 판단이다. 숫자는 측정값이 아닌
`DERIVED` 우선순위다.

| 단계 | 기능 | 사용자에게 보이는 결과 | 차별화 기여 | 부담 | 판단 |
| --- | --- | --- | ---: | ---: | --- |
| P0 | 지역 기록 3단계 | 방문 예정·사진·페이지 상태를 색과 진행률로 구분 | 높음 | 낮음 | 즉시 구현 |
| P0 | 지역 완성 카드 | 사진, 다이어리 수, 마지막 기록, 다음 행동을 표시 | 높음 | 낮음 | 즉시 구현 |
| P0 | 사진→페이지 연결 | 저장 뒤 기억 페이지 버튼으로 기존 에디터 이동 | 높음 | 낮음~중간 | 즉시 구현 |
| P1 | 지역 페이지 템플릿 | 언제·누구와·장면·재방문 의사를 짧게 기록 | 중간~높음 | 낮음 | P0와 함께 |
| P1 | 지역 완성 뱃지 | 사진과 페이지 조건을 모두 채울 때만 완성 | 중간 | 낮음 | 상태 모델 뒤 |
| P1 | 지역 공유 카드 | 지역 사진·한 줄·페이지 미리보기를 이미지로 공유 | 중간 | 낮음~중간 | 서버 없이 가능 |
| P2 | EXIF 보조 가져오기 | 촬영일·GPS로 후보 지역을 제안하고 사용자가 확정 | 낮음 | 중간 | 편의 기능 |
| P2 | 미방문 지역 추천 | TourAPI 관광지·사진을 보고 다음 지역으로 저장 | 중간 | 중간 | API 키 이후 |
| P2 | 연간 회고 | 완성 지역·페이지·재방문 기록을 한 장으로 표시 | 중간 | 중간 | 데이터 모델 이후 |
| P3 | 계정·클라우드 동기화 | 기기 변경 후 기록 복원 | 낮음(완성도 높음) | 높음 | 핵심 루프 이후 |

### P0 세부 설계

새 기능을 크게 만들기보다 현재 두 저장 흐름을 연결한다.

```text
RegionRecord
- regionCode
- visitState: WANT / PHOTO / DIARY_COMPLETE
- photoPath?
- diaryRecordIds
- firstRecordedAt?
- lastRecordedAt?
```

`PhotoStore`의 파일 존재 여부를 바로 버리지 말고, 먼저 `PHOTO` 상태를 계산하는
기존 방식과 호환한다. 다이어리 저장이 완료될 때 `DIARY_COMPLETE`를 계산한다.
대표 사진은 기존 설계처럼 자동 지정하지 말고 사용자가 버튼으로 선택하게 한다.
이렇게 하면 현재 기획 문서의 대표 사진 요구와 충돌하지 않는다.

### P1 회고 프롬프트

프롬프트는 AI가 아니라 고정 문구로 시작한다.

- “이 지역에서 가장 오래 기억하고 싶은 장면은?”
- “누구와 함께였나요?”
- “다시 간다면 무엇을 다르게 하고 싶나요?”
- “이 지역을 한 단어로 남긴다면?”

고정 문구는 네트워크·LLM 비용·개인정보 전송을 만들지 않고, 기존 텍스트 요소와
페이지 편집기를 바로 활용할 수 있다. AI 생성은 이 흐름이 실제로 사용되는지
확인한 뒤의 선택 사항이다.

## 6. 단계별 구현 로드맵

### 1단계 — 현재 구조를 살리는 로컬 MVP

1. 지역 상태 모델과 저장 포맷을 추가한다.
2. 지도 탭의 상세 영역에 사진·다이어리 진행 상태를 보여준다.
3. 사진 저장 완료 후 다이어리 만들기 버튼을 연결한다.
4. 다이어리 저장 완료 후 지도 상태와 완성 카드를 갱신한다.
5. 지역 공유 카드는 Android Share Intent와 로컬 Bitmap으로 처리한다.

**완료 기준:** 하나의 지역에서 사진을 채우고 페이지를 완성한 뒤, 앱을 다시
열어도 `DIARY_COMPLETE`와 페이지가 보인다.

### 2단계 — 기록 마찰 감소와 한국 콘텐츠

1. AndroidX `ExifInterface`로 날짜·GPS를 읽는다. 좌표가 없거나 틀릴 수 있으므로
   자동 확정하지 않고 후보와 수동 변경을 제공한다. [ExifInterface
   API](https://developer.android.com/reference/androidx/exifinterface/media/ExifInterface)
   는 EXIF 읽기·쓰기 API로 검색되지만, 모든 사진에 GPS가 있다는 뜻은 아니다.
2. 한국관광공사 API로 미방문 지역의 관광지·사진을 제공한다. 해당 API는 지역
   코드·관광 정보·이미지 정보를 JSON/XML REST로 제공한다고 안내한다. [공공데이터
   포털 [S-039]](https://www.data.go.kr/data/15101811/openapi.do)
3. API 결과는 “여행 계획”이 아니라 “다음 지역을 완성할 재료”로 제한한다.
4. 키·쿼터·라이선스 오류 시에도 기존 로컬 지도와 기록이 작동하도록 한다.

### 3단계 — 완성도와 서비스화

1. 지역·페이지·사진 모델에 대한 단위 테스트를 먼저 만든다.
2. ViewModel/Repository를 분리하고, 현재 로컬 파일을 감싸는 저장 경계를
   만든다.
3. 그 뒤 계정·클라우드 동기화를 도입한다. 현재 기획서가 Firebase Auth/Firestore/
   Storage를 추천하지만, 이는 차별화 기능이 아니라 데이터 보존 문제의 해결이다.
4. 백업·복원과 충돌 해결 정책을 정한 뒤에만 서버를 붙인다.

### 4단계 — 검증 후 선택

- 재방문 알림, 홈 위젯, 연간 회고: P0 루프 사용 데이터가 생긴 뒤 선택한다.
- AI 캡션·자동 일기: 고정 프롬프트가 부족하다는 사용 근거가 있을 때 선택한다.
- 실시간 위치 추적·친구 피드·리더보드: 배터리·권한·백엔드·운영 범위를
  감당할 때만 선택한다. Android 공식 가이드도 백그라운드 위치를 배터리와
  권한의 상충 관계로 다룬다. [배터리 가이드](https://developer.android.com/develop/sensors-and-location/location/battery)

## 7. 보류하거나 제외할 기능

| 기능 | 보류 이유 | 다시 검토할 조건 |
| --- | --- | --- |
| Firebase 인증·동기화 | 완성도는 높지만 차별화가 아니고, 업로드·보안 범위가 커진다. | 로컬 모델과 백업 포맷이 안정될 때 |
| 실시간 GPS 추적 | Polarsteps·FindPenguins와 경쟁하고 배터리·권한 부담이 생긴다. | 자동 추적 요구가 확인될 때 |
| AI 일기·장소 추정 | D·LOG·Journily와 겹치며 장소 오인식 위험이 있다. | 고정 프롬프트의 한계가 확인될 때 |
| 팔로우·댓글·리더보드 | 서버 운영이 필요하고 개인 기억 가치와 직접 연결되지 않는다. | 동행 기록 요구가 확인될 때 |
| 범용 일정·예약·예산 | Wanderlog가 이미 넓게 제공하는 영역이다. | 지역 미션으로 좁힐 수 있을 때 |
| 단순 통계·배지 추가 | 마이코리아맵·Been·Flyve 등에서 이미 흔하다. | 완성 상태와 결합한 지표가 필요할 때 |
| 로컬 저장만 강조 | 세 서비스도 오프라인·개인성을 강조한다. | 신뢰 설계 원칙으로 설명할 때 |

## 8. 검증·조사 방법과 한계

### 조사 축

한국 지도 제품, 자동 추적기, 방문·게이미피케이션, 일기·스크랩북, 여행 계획,
한국 공공 데이터·Android 구현 가능성, 차별화 반례를 분리해 조사했다.

### 증거 기준

- 제품 기능은 가능한 한 공식 제품 페이지·지원 문서·앱스토어 설명에서 확인했다.
- 앱스토어 문구와 업체 페이지는 **기능을 그렇게 설명한다는 증거**이지, 실제
  사용량·시장 점유율·사용자 만족도의 증거로 사용하지 않았다.
- “유일하다”, “국내 최초”, 다운로드 수, 시장 규모, 사용률은 검증하지 못했으므로
  결론에 넣지 않았다.
- 직접 확인이 안 된 동적 Android 문서는 검색 결과와 URL만 기록하고, 구현 세부
  단정은 피했다.
- 팀의 8개 멤버와 9개 보조 레인은 provider/API 설정 오류로 결과를 내지 못했다.
  그들의 실패를 연구 근거로 세지 않고, lead가 직접 검색·fetch·코드 검토를
  수행했다. 이 예외는 조사 세션 로그에 남겼다.

### 실행 검증

- `./gradlew :app:assembleDebug` — `BUILD SUCCESSFUL`
- `./gradlew testDebugUnitTest` — `BUILD SUCCESSFUL`; 프로젝트 문서상 테스트는
  사실상 스텁이므로 행동 커버리지를 의미하지 않는다.
- 소스 계약 확인 — `MapRegion`, `MapLoader`, `PhotoStore`, `DiaryStore`,
  `placeSaveAndReturn`, `ActivityResultContracts.GetContent` 존재를 assertion으로
  확인했다. 상세 결과는 [검증 기록][verify-baseline]에 있다.

### 남은 제품 검증

이 보고서는 시장 기능 비교이지 사용자 조사 결과가 아니다. 다음 가설은 실제
프로토타입에서 확인해야 한다.

1. 사진만 저장한 사용자가 페이지까지 완성하는가?
2. 상태가 세 단계일 때 지도 성취감이 단순 색칠보다 높아지는가?
3. 고정 프롬프트가 작성 부담을 줄이는가?
4. 공유 카드를 만들고 다시 앱으로 돌아오는가?

측정할 수 있는 제안 지표는 `지역 사진 기록→다이어리 완성 전환율`,
`완성 지역 수`, `페이지 재방문 수`, `공유 카드 생성 수`다. 이는
`PROPOSED` 지표이며 현재 사용자 데이터가 아니다.

[verify-baseline]: ../.omo/ulw-research/20260810-234110/verify-orme-baseline.md

## 9. 출처

출처는 조사 세션의 전체 원장 기준이다. 총 **50개 항목**, 웹 출처 **46개**,
웹 고유 호스트 **22개**, 1차 출처 표기 **42개**다. 같은 URL의 서로 다른 조사
용도도 원장에 별도 항목으로 남겼다.

### 프로젝트·구현

- [S-001 `handoff.md`](../project/handoff.md)
- [S-002 `project-ideas.md`](../project/project-ideas.md)
- [S-003 `AGENTS.md`](../../AGENTS.md)
- [S-004 `Diary.kt`](../../app/src/main/java/com/orme/app/ui/diary/Diary.kt)

### 지도·지역 기록

- [S-017 PhotoLog App Store](https://apps.apple.com/kr/app/%ED%8F%AC%ED%86%A0%EB%A1%9C%EA%B7%B8-%EC%A7%80%EB%8F%84-%EC%9C%84%EC%97%90-%EA%B8%B0%EB%A1%9D%ED%95%98%EB%8A%94-%EB%82%98%EB%A7%8C%EC%9D%98-%EC%97%AC%ED%96%89/id1195289279)
- [S-018 PhotoLog](https://www.photolog.kr/)
- [S-019 추억지도](https://www.memory-map.org/)
- [S-021 Flyve 초기 조사](https://flyve.kr/)
- [S-022 PhotoLog 직접 확인](https://www.photolog.kr/)
- [S-023 추억지도 직접 확인](https://www.memory-map.org/)
- [S-024 Flyve](https://flyve.kr/)
- [S-033 My Korea Map](https://play.google.com/store/apps/details?id=com.my.koreamap&hl=en-US)
- [S-034 여정](https://play.google.com/store/apps/details?id=com.travelmap.app&hl=en-US)
- [S-046 Travel Memory Map](https://apps.apple.com/us/app/travel-memory-map/id6478291625)

### 자동 추적·방문 지도

- [S-005 Polarsteps Travel Tracker](https://www.polarsteps.com/travel-tracker)
- [S-006 Polarsteps](https://www.polarsteps.com/)
- [S-007 Polarsteps tracking support](https://support.polarsteps.com/hc/en-us/articles/24266585115026-How-does-the-Travel-Tracker-work-Does-it-use-GPS)
- [S-008 FindPenguins](https://findpenguins.com/)
- [S-009 FindPenguins tracking support](https://support.findpenguins.com/hc/en-us/articles/360014713473-How-does-the-Travel-Tracker-work)
- [S-010 FindPenguins features](https://findpenguins.com/why-choose-findpenguins)
- [S-025 Polarsteps 직접 확인](https://www.polarsteps.com/travel-tracker)
- [S-026 FindPenguins 직접 확인](https://findpenguins.com/why-choose-findpenguins)
- [S-011 Been](https://been.app/)
- [S-012 Been map maker](https://been.app/map-maker)
- [S-027 Been 직접 확인](https://been.app/)
- [S-028 Been map maker 직접 확인](https://been.app/map-maker)
- [S-047 MemoMap](https://play.google.com/store/apps/details?id=com.nrk.memomap&hl=en-US)
- [S-048 MapMemories](https://play.google.com/store/apps/details?id=com.mapmemories.app&hl=en)

### 일기·스크랩북·계획

- [S-013 Day One features](https://dayoneapp.com/features/)
- [S-014 Day One guide](https://dayoneapp.com/guides/getting-started-with-day-one/getting-started/)
- [S-029 Day One 직접 확인](https://dayoneapp.com/features/)
- [S-030 Day One guide 직접 확인](https://dayoneapp.com/guides/getting-started-with-day-one/getting-started/)
- [S-020 D·LOG App Store](https://apps.apple.com/kr/app/%EB%94%94%EB%A1%9C%EA%B7%B8-%EC%82%AC%EC%A7%84-%EC%9D%BC%EA%B8%B0-%EC%97%AC%ED%96%89-%EA%B8%B0%EB%A1%9D-%ED%83%80%EC%9E%84%EB%9D%BC%EC%9D%B8-%EB%8B%A4%EC%9D%B4%EC%96%B4/id6451177080)
- [S-032 D·LOG](https://intro.dlog.me/)
- [S-035 Journily](https://journiapp.io/)
- [S-036 TripMemo](https://tripmemo.app/)
- [S-037 Safarnama](https://safarnamabymilan.com/)
- [S-049 TripMemo photo journal](https://tripmemo.app/travel-photo-journal-app)
- [S-015 Wanderlog](https://wanderlog.com/)
- [S-016 Wanderlog plan a trip](https://wanderlog.com/plan-a-trip)
- [S-031 Wanderlog 직접 확인](https://wanderlog.com/plan-a-trip)
- [S-050 MAPOG travel journal map](https://www.mapog.com/travel-journal-map/)

### 한국 공공 데이터·플랫폼

- [S-038 한국관광콘텐츠랩](https://api.visitkorea.or.kr/)
- [S-039 공공데이터포털 관광정보 API](https://www.data.go.kr/data/15101811/openapi.do)
- [S-040 VWorld](https://www.vworld.kr/v4po_main.do)
- [S-041 AndroidX ExifInterface](https://developer.android.com/reference/androidx/exifinterface/media/ExifInterface)
- [S-042 Android background location battery](https://developer.android.com/develop/sensors-and-location/location/battery)
- [S-043 Android Photo Picker](https://developer.android.com/training/data-storage/shared/photo-picker)
- [S-044 ML Kit image labeling](https://developers.google.com/ml-kit/vision/image-labeling/android)
- [S-045 Compose Glance widgets](https://developer.android.com/develop/ui/compose/glance/create-app-widget)

## 10. 조사 기록·검수 요약

- 조사 세션: `.omo/ulw-research/20260810-234110/`
- 확장 파동: 2회
- 별도 excursion: 0회
- claim graph: 10개 노드 — supported 8, partial 2, unresolved/refuted 0
- debate: 4회
- 실행 검증: baseline source assertion, `assembleDebug`, `testDebugUnitTest`
- 최종 파일: `docs/research/orme-differentiation-research.md`

이 보고서의 핵심 추천은 기능을 많이 추가하라는 것이 아니다. 현재 구현된 지도
사진과 다이어리 편집기를 **하나의 완료 루프**로 연결하고, 그 루프가 실제로
사용되는지 확인한 뒤에만 API·계정·AI를 추가하라는 것이다.
