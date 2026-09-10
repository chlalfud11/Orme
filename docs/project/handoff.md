# Orme 인수인계

작성 시점: 2026-08. 이전 작업은 Claude Code로 진행됨(로컬, git 미사용). 이 문서는 프로젝트를 **처음 보는 사람** 기준으로 씀. 확인 안 된 것은 "확인 필요"로 표기.

---

## 1. 프로젝트 개요
- **무엇**: "Orme" — 대한민국 지도를 여행 사진/다이어리로 채워나가는 안드로이드 앱. 지역(시군구)을 누르면 그 지역에 사진을 넣거나 다이어리(스티커·펜·텍스트·사진으로 꾸민 페이지)를 기록한다.
- **왜**: 여행 기억이 흩어지는 문제 → 지도를 하나씩 채우는 **시각적 성취(뿌듯함)**를 핵심 가치로. 전국이 내 사진으로 차오르는 느낌.
- **누구**: 개인 여행 기록용. **대학교 3학년 2학기 수업 프로젝트**로 사용 예정(아이디어 확장 목록은 `project-ideas.md` 참고).
- 현재는 **프론트엔드(안드로이드)만**. 서버/인증/DB 없음(전부 기기 로컬 저장).

## 2. 현재 상태
**완료(동작 확인됨)**
- 스플래시 → 로그인 → 회원가입 화면(디자인 시안 재현). ※ 인증 로직은 없음(UI만, 아무 값이나 진행).
- 지도: 줌아웃(전국 17 시도) ↔ 줌인(도별 시군). 도 탭 시 줌인. 광역시(7)+세종은 줌인 없이 단일 단위.
- 우상단 Region 드롭다운(시도/시군 목록으로 점프).
- 지역 탭 → **다이어리 화면**(기록 목록 + 기록 추가 + 대표 사진 버튼).
- 다이어리 만들기: 표지 선택 → 내지 선택 → 꾸미기 에디터(펜/텍스트/스티커/사진/undo·redo, 옆 스크롤로 페이지 추가) → 저장. 목록/뷰어에서 표지·페이지 확인.
- 대표 사진: 갤러리에서 사진 선택 → 드래그·핀치로 위치 조정 → 지역 모양으로 잘라 지도에 표시. 줌아웃 모자이크 + 도별 완성도 색농도.
- 사진 삭제/교체, 기록 삭제.

**미착수 / 향후(사용자 요청 예정)**
- 실제 표지·내지 디자인 에셋(현재 색상 6종/패턴 4종 플레이스홀더).
- 꾸미기 요소 개별 삭제/리사이즈/회전.
- 백엔드·실인증·클라우드 동기화, 통계/업적, 공공데이터(TourAPI 등) — `project-ideas.md`에 상세.
- 아키텍처 정비(MVVM/Repository/DI), 실제 테스트.

**진행 중**: 없음(직전 작업은 "기록이 대표사진으로 자동 반영되지 않도록" 수정 완료).

## 3. 아키텍처
단일 액티비티 + Compose. `MainActivity`(`enableEdgeToEdge`) → `OrmeTheme` → `OrmeNavGraph`.

디렉터리 (`app/src/main/java/com/orme/app/`):
- `MainActivity.kt` — 진입점.
- `navigation/NavGraph.kt` — 라우트 4개: `splash → login → signup → map`(Navigation Compose).
- `ui/splash/SplashScreen.kt` — 애니메이션 후 login으로.
- `ui/login/LoginScreen.kt`, `ui/signup/SignupScreen.kt` — 인증 UI(껍데기). 소셜버튼 등은 `ui/components/AuthComponents.kt`(`UnderlineField`, `SocialButtons`, `OrmeBottomBar`).
- `ui/theme/` — `Color.kt`(브랜드/지도 색), `Type.kt`(Playfair Display 번들 폰트), `Theme.kt`(`StatusBarIcons` 헬퍼 포함).
- `ui/map/`
  - `MapScreen.kt` (~570줄, **가장 큰 파일**) — 지도 렌더/탭/줌/드롭다운, 사진 편집기·다이어리 오버레이 호스팅.
  - `MapModels.kt` — `MapRegion`(Path + android Region 히트테스트), `MapData`, `SigunguIndex`, `MapLoader`(assets 파싱), `MapTransform`(viewBox↔픽셀).
  - `PhotoStore.kt` — 대표 사진 저장/크롭. `filesDir/map_photos/<code>.png`(파일 존재=사진 있음).
  - `PhotoPositionEditor.kt` — 사진 드래그/핀치 위치 조정(WYSIWYG). 좌표계=bounds-space.
- `ui/diary/`
  - `Diary.kt` — 모델(`DiaryElement`/`PageState`/`DiaryRecord`), `PageTemplate`/`CoverTemplate`, `DiaryStore`(저장/삭제), `renderPageToBitmap`/`renderCoverToBitmap`(android Canvas로 렌더).
  - `RegionDiaryFlow.kt` — 지역 다이어리 화면(목록/표지선택/내지선택/뷰어). 저장은 여기서.
  - `DecorateScreen.kt` — 꾸미기 에디터(create.png 스타일 툴바 + 캔버스).

**데이터 흐름**
- 지도 원본: `southkorea/southkorea-maps` GitHub GeoJSON(kostat 2018) → `scripts/build_map_paths.py`가 등거리투영·정규화·단순화 → `app/src/main/assets/map/`의 `provinces.json`(17 시도), `sigungu/<pc>.json`(도별 시군), `sigungu_index.json`(전국 프레임 모자이크/경계용). 앱은 `MapLoader`로 파싱, `PathParser`로 Compose Path 생성, `android.graphics.Region`으로 점-포함 히트테스트.
- 사진: 갤러리(GetContent) → `PhotoStore.decode` → `PhotoPositionEditor`에서 위치 지정 → `placeSaveAndReturn`(모양대로 크롭, PorterDuff SRC_IN) → `filesDir/map_photos/<code>.png`. 지도 채움/모자이크/완성도는 전부 이 PhotoStore 기반.
- 다이어리: 표지/내지 템플릿 선택 → `DecorateScreen`에서 요소를 데이터(`PageState`)로 관리 → 저장 시 `renderPageToBitmap`/`renderCoverToBitmap`으로 PNG 렌더 → `filesDir/diary/<code>/<recordId(timestamp)>/cover.png` + `page_00.png…`. **다이어리와 지도 대표사진은 완전히 별개 저장소**(중요, 5장 참고).

## 4. 개발 환경 세팅
1. Android Studio 설치(에뮬레이터 API 26+; 개발은 Pixel 8 / API 34~35 에뮬레이터에서 진행됨).
2. 프로젝트 열기(Gradle sync). **최초 빌드는 인터넷 필요**(의존성 다운로드).
3. 빌드/실행/설치 명령은 `AGENTS.md` 참고. 빌드에 Android Studio 번들 JDK 지정 필수(`JAVA_HOME=/Applications/Android Studio.app/Contents/jbr/Contents/Home`).
4. 지도 재생성이 필요할 때만:
   - `curl -sL -o /tmp/kr_prov.json <southkorea-maps의 skorea-provinces-2018-geo.json>`
   - `curl -sL -o /tmp/kr_muni.json <southkorea-maps의 skorea-municipalities-2018-geo.json>`
   - `python3 scripts/build_map_paths.py` (PIL 필요: `preview_map.py`로 미리보기).
5. **환경변수/시크릿**: 현재 앱은 API 키/토큰/`.env` **없음**. (향후 공공데이터 연동 시 필요할 값은 아래 "변수명·용도"만. 실제 키는 절대 커밋 금지.)
   - `DATA_GO_KR_SERVICE_KEY` — (향후) 공공데이터포털 서비스키. 현재 미사용.
   - Firebase 도입 시 `google-services.json` — (향후) 앱 모듈에 위치. 현재 없음.

## 5. 주요 의사결정과 이유 (코드만으로는 알 수 없는 맥락 — 가장 중요)
- **지도를 지도 SDK/벡터 드로어블이 아니라 Compose Canvas + 도별 Path JSON으로 구현.** 이유: 지역별 개별 클릭(히트테스트) + 지역 모양대로 사진 크롭이 핵심이라 커스텀 렌더가 필요. (사용자가 "Canvas + 도별 경로 데이터 파일" 선택.)
- **투영**: 등거리 근사 + 위도보정 `KX=cos(36.5°)`로 경도 축소. 극동 섬(lon>130.2)은 줌아웃 bbox에서 제외해 본토 크기 유지.
- **광역시/세종은 줌인 없이 단일 단위**(사용자 결정). `NO_ZOOM_CODES={11,21,22,23,24,25,26,29}`는 시군구 파일/인덱스를 아예 생성하지 않고, 줌아웃에서 도형을 바로 눌러 사진/다이어리. 내부 구/동 경계 제거.
- **줌인 단순화 eps**: 일반 도 `0.004`, 광역시 `0.0004`. 광역시는 작은 구가 많아 0.004면 인접 구 공유경계가 어긋나 틈/겹침 발생 → 정밀화. (독립 Douglas–Peucker의 한계를 eps 축소로 회피.)
- **인천 옹진군 제외**: 옹진이 백령~영흥까지 경도폭 1.89°로 흩어져 본토 bbox를 왜곡 → 광역시 한정 경도폭>0.9° 시군은 지도에서 제외(사용자 결정).
- **대표사진(지도) ↔ 다이어리(기록) 분리**: 지도 채움은 `PhotoStore`(지역모양 크롭)로만. 기록은 `DiaryStore`. **기록을 만들어도 지도에는 자동으로 안 뜨고**, "대표 사진" 버튼으로 직접 지정할 때만 채워진다(사용자 최신 요구).
- **사진은 자동 중앙크롭이 아니라 수동 위치 조정**(`PhotoPositionEditor`). 사용자가 "사진을 맘대로 넣지 말고 손가락으로 위치 조정"을 명시.
- **폰트**: 로그인/회원가입 제목은 번들 **Playfair Display**(고대비 세리프)로 디자인 재현. 디자인이 더 넓적해서 `textGeometricTransform(scaleX=1.18)`로 폭 보정. 제목 밑단이 주황 카드 상단에 얹히도록 z-순서 배치. (Region 버튼/도·시군 텍스트는 반대로 세리프를 빼고 기본 산세리프.)
- **꾸미기 저장 방식**: 요소를 비트맵 캡처가 아니라 **데이터로 보관 후 android Canvas로 재렌더**(`renderPageToBitmap`). 멀티페이지·정확 좌표 재현에 유리.

## 6. 시도했다가 폐기한 접근 (반복 삽질 방지)
- 사진 **자동 중앙크롭** → 폐기, 수동 `PhotoPositionEditor`로 대체.
- 광역시 시군 `eps=0.004` → 구들이 삐뚤·겹침 → `0.0004`.
- 인천 옹진 **인셋 박스**로 분리 → 결국 **완전 제외**(사용자 선택).
- 최초 기록 저장 시 **대표사진 자동 설정(onFirstCover)** → 제거(사용자: 대표사진은 버튼으로만).
- 표지 선택을 한때 빼고 내지만 → 다시 **표지 선택 부활**(사용자 요구 번복). 현재 표지 있음.
- 꾸미기 툴바를 **이모지 아이콘**으로 → create.png와 달라 **벡터 라인 아이콘**(ic_pen/sticker/image/undo/redo)으로 교체. 하단 네비바도 제거(전체화면).
- 스티커가 안 들어가던 버그: 이모지 `Text.clickable` 탭영역이 너무 작았고 `currentPage()`가 null 가능 → **46dp Box 버튼 + currentPage 견고화**로 해결.
- 에뮬레이터에 사진 주입: `run-as`/tar-pipe 방식 실패 → `adb push /sdcard/Pictures/` + 미디어스캔이 정답. HEIC는 `sips`로 JPEG 변환 후 push.

## 7. 알려진 이슈 / 기술 부채
- **데드코드**: `ui/map/PhotoStore.kt` `cropToRegion`(64), `cropSaveAndReturn`(149) — 현재 호출처 없음(대표사진은 `placeSaveAndReturn`만 사용). / `ui/map/MapScreen.kt` `dialogCode` 상태(82)와 교체·삭제 `AlertDialog`(427~)는 다이어리 도입 후 발동 안 됨(never non-null). 정리 대상.
- **미사용 심볼**: `ui/signup/SignupScreen.kt` `OrmeBottomBar` 임포트(32)·`onMapClick` 파라미터(46) — 네비바 제거 후 미사용(린트 경고).
- **테스트 없음**: `app/src/*/…/ExampleUnitTest.kt`, `ExampleInstrumentedTest.kt` 예제 스텁뿐. 회귀 검증은 수동(에뮬레이터).
- **인증 미구현**: 로그인/회원가입은 UI만. 아무 입력이나 통과, 검증·세션 없음.
- **git 미초기화**: 버전관리 없음(히스토리·브랜치 없음). 인수인계 시 `git init` 권장. 루트 `.gitignore`는 안드로이드 기본 템플릿이라 `/build`·`.gradle`·`.idea` 일부만 커버하고 **`app/build/`, `.omc/`, `my_photos/`, `.kotlin/`는 누락** → git init 시 이들을 추가해야 생성물/개인사진이 커밋되지 않는다.
- **버전 특이**: AGP 9.3.1 / compileSdk·targetSdk 37 / composeBom 2026.02.01 등 매우 최신·프리뷰급. 다른 머신에서 재현 시 SDK/AGP 설치 확인 필요.
- **adb 경로**: 시스템 PATH에 없어 풀경로(`~/Library/Android/sdk/platform-tools/adb`) 사용.

## 8. 다음 작업 (우선순위 순)
1. **데드코드/미사용 심볼 정리** — 파일: `PhotoStore.kt`(cropToRegion·cropSaveAndReturn), `MapScreen.kt`(dialogCode+AlertDialog), `SignupScreen.kt`(미사용 임포트/파라미터). 완료 기준: 빌드 경고 감소, 기능 회귀 없음(에뮬 확인).
2. **꾸미기 요소 개별 조작** — 파일: `DecorateScreen.kt`, `Diary.kt`. 선택된 요소 삭제/리사이즈/회전, redo 스택은 이미 있음. 완료 기준: 요소 탭→핸들로 조작 가능.
3. **실제 표지/내지 디자인 에셋 적용** — `Diary.kt`의 `CoverTemplate`/`PageTemplate` 렌더를 실제 이미지 기반으로. 완료 기준: 플레이스홀더 대체.
4. **아키텍처 정비(MVVM+Repository)** — 상태를 ViewModel로, 저장 접근을 Repository로. 완료 기준: `MapScreen` 등 화면 로직 분리, 동작 동일.
5. **백엔드/인증 등** — `project-ideas.md`의 권장안(Firebase 인증·저장 → 여행일지 → 통계 → TourAPI). 완료 기준은 항목별 정의 필요.

## 9. 외부 연동
- **현재 없음**(오프라인 로컬 앱). 네트워크 권한도 매니페스트에 없음.
- 지도 원본 데이터: `southkorea/southkorea-maps`(GitHub 공개, kostat 2018 GeoJSON) — 재생성 시에만 필요.
- 갤러리: 안드로이드 PhotoPicker(`ActivityResultContracts.GetContent`). 별도 키 불필요.
- (향후) 공공데이터포털 API 목록·용도는 `project-ideas.md` 참고. 접근에는 서비스키 필요(이 문서 4장 변수명 참고).

---
### 참고: Claude 세션이 남긴 프로젝트 메모(코덱스에는 자동 로드 안 됨)
`~/.claude/projects/-Users-miryeong-AndroidStudioProjects-Orme/memory/` 에 `orme-map-pipeline.md`, `orme-diary-feature.md`가 있음. 핵심은 위 3·5장에 녹였으나, 원문이 필요하면 그 경로 참조(민감정보 없음).
