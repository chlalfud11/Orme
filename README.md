# Orme

대한민국 지도를 여행 사진과 다이어리로 채워가는 Android 여행 기록 앱입니다.
여행한 지역을 사진으로 채우고, 그 지역에 다이어리를 남기면서 전국 여행 기록을
한눈에 확인할 수 있도록 만드는 것이 이 프로젝트의 핵심입니다.

## 프로젝트 소개

Orme는 여행 기록이 여러 앱과 사진첩에 흩어지는 문제를 해결하기 위한 수업 프로젝트입니다.
지도를 중심 화면으로 사용해 지역별 여행 기록을 시각적으로 쌓아가는 경험을 제공합니다.

- 앱 종류: Kotlin 기반 Android 앱
- UI: Jetpack Compose 및 Material 3
- 저장 방식: 기기 내부 파일 저장
- 서버, 회원 인증, 데이터베이스, 클라우드 동기화: 현재 미사용

## 주요 기능

### 지도

- 대한민국 17개 시·도를 지도에서 확인
- 시·도를 누르면 해당 지역의 시·군·구 지도로 확대
- 지역을 선택해 해당 지역의 다이어리 화면으로 이동
- 지역별 대표 사진을 등록하고 지역 모양에 맞게 잘라 지도에 표시
- 사진이 등록된 지역은 줌아웃 화면에서 모자이크와 완성도 색상으로 표시
- 지도 데이터는 지도 SDK 대신 Compose Canvas와 생성된 Path JSON을 사용

### 다이어리

- 지역별 다이어리 목록 확인
- 표지와 내지 템플릿 선택
- 펜, 텍스트, 스티커, 사진으로 페이지 꾸미기
- 여러 페이지 작성
- 실행 취소와 다시 실행
- 다이어리 저장, 조회, 삭제

### 화면 흐름

```text
스플래시 → 로그인 → 회원가입 → 지도
                         ↓
                  지역 선택 → 다이어리
```

로그인과 회원가입 화면은 현재 UI 중심으로 구현되어 있으며 실제 인증 기능은
연결되어 있지 않습니다.

## 기술 스택

| 항목 | 버전 또는 내용 |
| --- | --- |
| Kotlin | 2.2.10 |
| Android Gradle Plugin | 9.3.1 |
| Gradle | 9.5.0 |
| Jetpack Compose BOM | 2026.02.01 |
| Material 3 | 사용 |
| Navigation Compose | 2.8.5 |
| Java | 11 |
| compileSdk / targetSdk | 37 |
| minSdk | 26 |

## Android Studio 에뮬레이터 환경

로컬 Android Studio에 설정되어 실제 개발에 사용한 AVD는 다음과 같습니다.

| 에뮬레이터 이름 | AVD ID | Android / SDK | 이미지 |
| --- | --- | --- | --- |
| Pixel 8 | `Pixel_8` | Android 15 / API 35 | Google Play, arm64-v8a |
| Medium Phone | `Medium_Phone` | Android 15 / API 35 | Google Play, arm64-v8a |

에뮬레이터의 테스트 SDK는 API 35이고, 프로젝트를 빌드하는 compileSdk와
targetSdk는 37입니다. 팀원의 에뮬레이터 이름은 달라도 API 26 이상의
에뮬레이터에서 실행할 수 있습니다.

## 시작하기

1. Android Studio에서 이 저장소를 엽니다.
2. Gradle 동기화를 완료합니다. 최초 빌드에는 인터넷 연결이 필요합니다.
3. API 26 이상의 에뮬레이터를 실행합니다.
4. 아래 명령으로 빌드합니다.

```bash
./gradlew :app:assembleDebug
```

Android Studio 번들 JDK를 사용해야 하는 환경에서는 다음처럼 실행합니다.

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :app:assembleDebug
```

## 프로젝트 구조

```text
app/src/main/java/com/orme/app/
├── navigation/       화면 이동과 라우트
├── ui/components/    공용 Compose 컴포넌트
├── ui/diary/         다이어리 모델, 저장, 에디터
├── ui/map/           지도 렌더링, 지역 선택, 대표 사진
├── ui/login/         로그인 화면
├── ui/signup/        회원가입 화면
├── ui/splash/        스플래시 화면
└── ui/theme/         색상, 글꼴, 테마
```

- `MainActivity`가 앱의 진입점입니다.
- `navigation/NavGraph.kt`가 스플래시부터 지도까지의 화면 전환을 담당합니다.
- `ui/map/MapLoader`가 `app/src/main/assets/map/`의 지도 Path JSON을 읽습니다.
- `PhotoStore`는 대표 사진을 `filesDir/map_photos/`에 저장합니다.
- `DiaryStore`는 다이어리를 `filesDir/diary/`에 저장합니다.
- 지도 대표 사진과 다이어리 사진은 서로 다른 저장소를 사용합니다.

## 팀 작업 방법

`main`에는 확인된 코드를 모으고, 기능별 브랜치를 만들어 작업합니다.

```bash
git switch main
git pull origin main
git switch -c feature/<기능명>
```

작업이 끝나면 커밋하고 GitHub에서 `main`을 대상으로 Pull Request를 생성합니다.
팀원 리뷰 후 병합한 다음, 다음 작업 전에 `main`을 다시 pull합니다.

브랜치 이름 예시:

- `feature/map`
- `feature/diary`
- `feature/login`
- `fix/photo-crop`

## 현재 제한 사항

- 로그인과 회원가입은 화면만 있고 실제 인증은 없습니다.
- 데이터는 현재 사용하는 기기에만 저장됩니다.
- 표지와 내지 일부 디자인은 플레이스홀더 템플릿입니다.
- 백엔드, 클라우드 동기화, 통계와 업적 기능은 향후 확장 대상입니다.

## 참고 문서

- [프로젝트 구조](docs/README.md)
- [현재 상태와 아키텍처 인수인계](docs/project/handoff.md)
- [기능 확장 아이디어](docs/project/project-ideas.md)
- [디자인 규약](DESIGN.md)
- [작업 규칙](AGENTS.md)
