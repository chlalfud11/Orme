# AGENTS.md — Orme

한국 여행기록 안드로이드 앱(지도를 사진/다이어리로 채우는 앱). **Kotlin + Jetpack Compose(Material3) 단일 액티비티, 로컬 저장만(백엔드 없음).**

> 이 파일은 매 세션 컨텍스트에 실린다. 간결 유지. 배경/맥락은 `docs/project/handoff.md`, 코덱스 이전 관련은 `docs/tooling/codex-migration.md` 참고.

## 기술 스택
- Kotlin 2.2.10, Jetpack Compose (BOM `2026.02.01`), Material3, Navigation Compose 2.8.5
- minSdk 26 / target·compile 37, Java 11, Gradle 9.5.0, AGP 9.3.1, `kotlin.code.style=official`
- DI/DB/네트워크 라이브러리 **없음**(Hilt/Room/Retrofit 미사용). 저장은 `context.filesDir` 파일.
- 지도 데이터는 Python으로 GeoJSON→Path JSON 생성해 `app/src/main/assets/map/`에 둠.

## 빌드 / 실행 / 테스트 / 린트 (복사해서 바로 실행)
빌드에는 Android Studio 번들 JDK가 필요하다. 매 명령 앞에 아래 2줄을 먼저 실행:
```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```
- 디버그 빌드: `./gradlew :app:assembleDebug`
- 설치(에뮬레이터/기기 실행 중): `~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk`
- 앱 실행: `~/Library/Android/sdk/platform-tools/adb shell monkey -p com.orme.app -c android.intent.category.LAUNCHER 1`
- 단위 테스트: `./gradlew testDebugUnitTest`  (※ 현재 예제 스텁만 존재)
- 계측 테스트: `./gradlew connectedDebugAndroidTest`  (기기 필요, 예제 스텁만)
- 린트: `./gradlew :app:lintDebug`
- 지도 데이터 재생성: `python3 scripts/build_map_paths.py` (사전에 `/tmp/kr_prov.json`,`/tmp/kr_muni.json` 필요 — HANDOFF 참고)

`adb`는 `~/Library/Android/sdk/platform-tools/adb`. 시스템 PATH에 없을 수 있음.

## 코딩 컨벤션
- Kotlin official 스타일(4-space indent). 파일당 하나의 화면/책임.
- 패키지 `com.orme.app`. UI는 **기능별** 디렉터리: `app/src/main/java/com/orme/app/ui/<feature>/` (login, signup, map, diary, components, theme, splash).
- 단일 액티비티(`MainActivity`) → `navigation/NavGraph.kt`(Navigation Compose). 상태는 컴포저블에 hoist(아직 ViewModel/Repository/DI 없음).
- 색상은 전부 `ui/theme/Color.kt` 상수 사용. 폰트는 `ui/theme/Type.kt`.
- 주석은 한국어(기존 코드 스타일에 맞출 것).
- 임포트 순서: android/androidx/compose → 프로젝트(`com.orme.*`). 와일드카드는 기존에 쓰인 곳(`foundation.layout.*`)만 허용.
- 커밋 메시지 형식: **git 미초기화 상태(아래 참고)라 확립된 규칙 없음.** git을 init한다면 짧은 명령형 제목 권장(확인 필요).

## 절대 하면 안 되는 것
- `app/src/main/assets/map/*.json` **직접 편집 금지.** `scripts/build_map_paths.py`가 생성하는 산출물. 지도 수정은 스크립트를 고치고 재생성.
- `res/`에 **공백/특수문자 들어간 파일명 금지.** 참고 디자인 PNG(공백 있는 파일)는 `res/`에 넣으면 `packageDebugResources` 실패 → `design/`에 보관.
- 생성물 디렉터리 커밋/편집 금지: `build/`, `.gradle/`, `.kotlin/`, `.omc/`(oh-my-claudecode 상태), `.idea/`.
- `my_photos/` = 사용자 개인 사진 폴더(앱 코드 아님). 건드리지 말 것.
- 무거운 의존성(Hilt/Room/Retrofit 등) 임의 추가 금지 — 지금은 의도적으로 dependency-light. 도입은 `docs/project/handoff.md`의 "다음 작업"에서 합의 후.
- 배포/서명 설정 없음(릴리스 미구성). 릴리스 관련 파일 생성/변경 금지.

## 작업 완료 전 필수 검증
1. `./gradlew :app:assembleDebug` 성공(경고는 무방, 에러 0).
2. **자동 테스트가 사실상 없으므로**, 변경한 화면/흐름을 에뮬레이터에 설치해 직접 구동 확인(`adb install` 후 해당 플로우 조작). 지도/사진/다이어리처럼 시각적 기능은 스크린샷으로 확인(`adb exec-out screencap -p > out.png`).
