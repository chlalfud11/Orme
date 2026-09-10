# Orme 프로젝트 구조

이 문서는 소스, 런타임 에셋, 디자인 참고 자료, 개발 문서를 어디에 둘지 정리한 안내서입니다.

## 최상위 구성

| 경로 | 용도 |
| --- | --- |
| `app/` | Android 앱 모듈과 실제 런타임 리소스 |
| `design/` | 앱에 포함하지 않는 디자인 시안과 브랜드 참고 자료 |
| `docs/` | 인수인계, 기획, 조사, 개발 도구 안내 |
| `scripts/` | 지도 데이터 생성과 개발용 보조 스크립트 |
| `gradle/`, `gradlew*`, `*.gradle.kts`, `gradle.properties` | Gradle 빌드 설정과 래퍼 |
| `AGENTS.md` | 프로젝트 작업 규칙. 도구가 자동으로 찾으므로 루트에 유지 |
| `DESIGN.md` | 디자인 시스템 계약. 디자인 작업의 기준 파일이라 루트에 유지 |
| `README.md` | 프로젝트 진입점과 주요 문서 링크 |

## 앱 모듈: `app/`

- `src/main/java/com/orme/app/`: 기능별 Compose 화면과 앱 로직
  - `navigation/`: 화면 전환
  - `ui/components/`: 여러 화면에서 쓰는 컴포넌트
  - `ui/{diary,login,map,profile,search,signup,splash,theme}/`: 기능별 화면과 상태
- `src/main/assets/map/`: 생성 스크립트가 만드는 지도 Path JSON. 직접 수정하지 않습니다.
- `src/main/assets/search/`: 여행지 추천 화면에서 사용하는 이미지와 출처 정보.
- `src/main/res/`: drawable, 폰트, 문자열 등 Android 리소스.
- `src/test/`, `src/androidTest/`: 단위·계측 테스트.

`app/src/main/assets/`에는 앱이 실제로 읽는 자료만 둡니다. 로그인 시안은 런타임에서 사용하지 않아 `design/references/login/`으로 분리했습니다.

## 디자인 자료: `design/`

- `brand/logo-options/`: 로고 후보.
- `references/login/`: 로그인 화면 시안.
- `references/profile/`: 프로필 화면 시안.
- `references/map/`: 지도 화면 시안.
- `references/diary/`: 다이어리 관련 시안.
- `references/search/`: 검색·위시리스트 시안.
- `references/inspiration/`: 번호만 있던 참고 이미지 묶음.

디자인 자료는 앱 리소스가 아닙니다. 배포에 포함할 파일은 `app/src/main/res/` 또는 `app/src/main/assets/`에만 넣습니다.

## 개발 문서: `docs/`

- `project/handoff.md`: 현재 구현 상태, 구조, 의사결정, 기술 부채.
- `project/project-ideas.md`: 이후 기능 확장 아이디어.
- `research/orme-differentiation-research.md`: 경쟁·차별화 조사.
- `tooling/codex-migration.md`: Codex 환경으로 이어받기 위한 안내.
- `tooling/codex-start-prompt.md`: Codex 첫 세션용 시작 프롬프트.

## 로컬·생성 경로

다음은 앱 소스가 아니므로 정리 대상에서 제외합니다. 이동하거나 편집하지 않습니다.

- `build/`, `.gradle/`, `.kotlin/`: Gradle·Kotlin 생성물과 캐시
- `.idea/`: Android Studio 설정
- `.omo/`, `.senpi/`: 작업 도구 런타임 상태
- `my_photos/`: 개인 사진
- `local.properties`: 로컬 Android SDK 경로
- `.DS_Store`: macOS Finder 메타데이터 (`.gitignore`로 제외)

## 새 파일을 둘 곳

1. 앱에서 읽는 코드·리소스는 `app/src/main/` 아래에 둡니다.
2. 앱에 포함하지 않는 시안은 `design/brand/` 또는 `design/references/<기능>/`에 둡니다.
3. 사람이 읽는 문서는 `docs/project/`, `docs/research/`, `docs/tooling/` 중 성격에 맞는 곳에 둡니다.
4. 지도 JSON은 `scripts/build_map_paths.py`로만 다시 만듭니다.
