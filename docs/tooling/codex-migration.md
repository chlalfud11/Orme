# Codex 이전 안내

이 프로젝트를 Codex CLI로 이어받을 때 필요한 것만 정리. (Codex는 `AGENTS.md`를 자동으로 읽지만 `CLAUDE.md`/`.claude/`는 읽지 않는다.)

## TL;DR
- 프로젝트에 **committed 된 Claude 전용 설정은 없다.** 프로젝트 규칙은 `AGENTS.md`, 맥락은 `docs/project/handoff.md`에 정리했다. Codex는 그 둘만 보면 된다.
- **프로젝트가 요구하는 MCP 서버 없음.** (아래 참고)
- 권장: 샌드박스 `workspace-write` + 승인 `on-request` + **네트워크 허용**(첫 빌드/지도 재생성에 필요).

## 1. Claude(.claude/·OMC) 기능 → Codex 대체
| Claude 쪽 | 이 프로젝트에서의 실체 | Codex 대체 |
|---|---|---|
| 프로젝트 `.claude/`, 프로젝트 `CLAUDE.md` | **존재하지 않음** (전역 `~/.claude/CLAUDE.md`만 있고 그건 oh-my-claudecode 오케스트레이션용, 프로젝트 규칙 아님) | 대체 불필요. 규칙은 `AGENTS.md`로 이관 완료 |
| 커스텀 슬래시 커맨드 | 프로젝트에 없음 | 필요 시 `~/.codex/prompts/<name>.md`(프롬프트 파일)로 만들면 `/name`으로 호출 가능 (확인 필요: 설치된 Codex 버전의 프롬프트 경로) |
| 스킬 / 훅 / 서브에이전트 지시 | 프로젝트에 committed 된 것 없음. 이전 세션이 쓰던 OMC 스킬/훅은 전역 설정이라 프로젝트와 무관 | 대체 불필요. `AGENTS.md`의 평문 지시로 충분 |
| Claude 프로젝트 메모리 | `~/.claude/projects/-Users-miryeong-AndroidStudioProjects-Orme/memory/`(`orme-map-pipeline.md`, `orme-diary-feature.md`) | Codex는 자동 로드 안 함. 핵심은 `docs/project/handoff.md`에 녹임. 원문 필요하면 그 경로 직접 읽기 |
| `.omc/` 디렉터리(루트, `my_photos/.omc`) | oh-my-claudecode 런타임 상태(세션/체크포인트) | **무시/삭제 가능.** 앱과 무관. (현재 `.gitignore`엔 미포함 → git init 시 `.omc/` 추가 권장) |

> 결론: "옮겨야 하는" Claude 전용 자산은 사실상 없다. `AGENTS.md` + `docs/project/handoff.md`가 인수인계 본체다.

## 2. MCP 서버
- **이 프로젝트는 MCP 서버를 요구하지 않는다.** (앱은 오프라인 로컬, 코드/빌드에 외부 MCP 불필요.)
- 이전 Claude 세션에서 보였던 MCP(oh-my-claudecode 툴, Google Drive 등)는 **사용자의 Claude Code 개인 환경** 설정이지 프로젝트 의존성이 아니다. 옮길 필요 없음.
- 만약 Codex에서 편의 기능을 붙이고 싶다면(선택), `~/.codex/config.toml`에 예:
  ```toml
  # (선택) 파일시스템 접근 MCP 예시 — 프로젝트 필수 아님
  # [mcp_servers.filesystem]
  # command = "npx"
  # args = ["-y", "@modelcontextprotocol/server-filesystem", "/Users/miryeong/AndroidStudioProjects/Orme"]
  ```
  (정확한 키/서버는 설치 버전 문서 확인 필요.)

## 3. 권장 승인 모드 · 샌드박스
이 프로젝트 작업에는 (a) `./gradlew` 빌드가 **레포 안 `build/`에 쓰기**, (b) **첫 빌드 시 네트워크**(의존성 다운로드), (c) `adb`로 에뮬레이터 제어, (d) 지도 재생성 시 `curl`로 GeoJSON 다운로드 + `python3` 실행이 필요하다.

권장 (`~/.codex/config.toml`, 키 이름은 설치 버전 기준으로 확인):
```toml
approval_policy = "on-request"      # 필요할 때 에스컬레이션 요청
sandbox_mode    = "workspace-write" # 레포 내 쓰기 허용(build 산출물)

[sandbox_workspace_write]
network_access = true               # 첫 Gradle 의존성 다운로드 / GeoJSON fetch 에 필요
```
이유:
- **network_access = true 필수 케이스**: 최초 `:app:assembleDebug`는 Compose/AGP 의존성을 받으므로 네트워크 없으면 실패. 지도 재생성(`build_map_paths.py` 전 단계 `curl`)도 네트워크 필요. (이미 `~/.gradle` 캐시가 채워진 상태면 오프라인 빌드 가능하지만, 새 머신 가정하면 켜는 게 안전.)
- `workspace-write`: `./gradlew`가 `build/`, `.gradle/`에 써야 하므로 read-only로는 빌드 불가.
- `adb`: 에뮬레이터와 `127.0.0.1:5037`로 통신. 샌드박스가 loopback을 막으면 `adb` 단계에서 승인/에스컬레이션 필요할 수 있음(확인 필요). 막히면 해당 명령만 사용자 승인으로 실행.
- 완전 자동(`danger-full-access`)은 권장하지 않음 — 빌드/삭제 명령이 많아 사고 위험. `on-request`로 사람이 확인.

## 4. Codex가 첫 세션에 반드시 알아야 할 것
- `AGENTS.md`(규칙·명령어), `docs/project/handoff.md`(맥락·의사결정·다음 작업), `docs/project/project-ideas.md`(향후 기능·공공데이터)를 먼저 읽기.
- **git 미초기화** 상태 — 변경 이력 없음. 큰 변경 전 `git init` 후 커밋 권장(`.gitignore` 존재).
- 자동 테스트가 없으니 **에뮬레이터로 직접 구동 검증**이 사실상의 테스트다.
- `app/src/main/assets/map/*.json`은 생성물 — 직접 편집 금지(스크립트로 재생성).

---

## 5. 코덱스 첫 세션 시작 프롬프트
아래 블록을 Codex CLI 첫 메시지로 그대로 붙여넣으면 된다. (`docs/tooling/codex-start-prompt.md`에도 동일 내용 저장됨.)

```
너는 이제 "Orme"라는 안드로이드(Kotlin/Jetpack Compose) 앱을 이어서 개발한다.
이 프로젝트는 Claude Code에서 넘어왔고, git은 아직 초기화돼 있지 않다.

시작 전에 다음 파일을 순서대로 읽고 요약해라(추측 금지, 코드에서 확인되는 것만):
1) /Users/miryeong/AndroidStudioProjects/Orme/AGENTS.md   (규칙·빌드/실행/테스트 명령·금지사항)
2) /Users/miryeong/AndroidStudioProjects/Orme/docs/project/handoff.md   (개요·현재상태·아키텍처·의사결정·다음작업)
3) /Users/miryeong/AndroidStudioProjects/Orme/docs/project/project-ideas.md (향후 기능/공공데이터)

읽은 뒤 나에게 이걸 확인해줘:
- 프로젝트 한 줄 요약과 기술 스택
- 지금 바로 빌드가 되는지 점검 계획 (JAVA_HOME 지정 → ./gradlew :app:assembleDebug)
- HANDOFF "8. 다음 작업" 중 1순위(데드코드 정리)를 첫 작업으로 제안하되, 착수 전 내 승인을 받아라

작업 원칙:
- 빌드는 반드시 Android Studio 번들 JDK로:
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"; export PATH="$JAVA_HOME/bin:$PATH"
- adb는 풀경로 사용: ~/Library/Android/sdk/platform-tools/adb
- app/src/main/assets/map/*.json 은 생성물이니 직접 수정하지 말 것(scripts/build_map_paths.py로 재생성).
- 자동 테스트가 없으므로, 화면/기능을 바꾸면 에뮬레이터에 설치해 직접 구동하고 스크린샷으로 확인해라.
- 확실하지 않으면 코드/파일을 먼저 확인하고, 그래도 모르면 "확인 필요"라고 말하고 나에게 물어라.

먼저 위 3개 파일을 읽고 요약 + 빌드 점검 계획부터 제시해라. 코드 변경은 내 승인 후에 시작.
```
