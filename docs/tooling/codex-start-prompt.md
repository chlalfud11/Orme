# 코덱스 첫 세션 시작 프롬프트

아래 내용을 OpenAI Codex CLI 첫 메시지로 그대로 붙여넣으세요.

---

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
