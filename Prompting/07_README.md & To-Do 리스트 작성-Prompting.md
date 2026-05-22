# 07. README.md & To-Do 리스트 작성 Prompting

## 1. 목적

이 문서는 `feedback_analyzer_A02` 저장소를 대상으로 이번 세션에서 수행한 README 개편, Phase 5 To-Do 리스트 작성, README와 PRD 비교 보고의 사용자 프롬프트와 해석 기준을 보관하기 위한 Prompting 기록이다.

핵심 목표는 Phase 5 PRD의 기능 요구사항과 인수 기준을 기반으로 문서 산출물을 작성하는 것이다. 코드 구현은 포함하지 않는다.

## 2. 세션 프롬프트 원문

### Prompt 1. README.md 작성 요구

```text
워크스페이스: @c:\dev\feedback_analyzer_A02\ 
저장소: https://github.com/jh8710/feedback_analyzer_A02.git
전제: Phase 5 PRD를 기반으로 작성하라. ⚠️ 코드 구현 추가 금지.

# DemoApplication.java
한 줄 설명 — PRD 1.1 목적문 활용

## 목차 (자동 링크 포함)

## 개요 (Overview)
- 이 프로젝트가 해결하는 문제 (PRD 1.2 배경 기반)
- 주요 학습 목표 (OCP/SRP, BCE, TDD)
- 현재 코드의 문제점과 개선 방향

## 빠른 시작 (Quick Start)
- 사전 조건 (Java 버전, 빌드 도구)
- 빌드 & 실행:  mvn spring-boot:run
- 예시 입출력: 복숭아 좋다 → 결과

## 입력 형식 계약
- 정상: 키워드,감정 포함한 입력 예시 3개
- 비정상: 특수문자만 있음, 내용 null 케이스 1개씩 + 에러 메시지

## 아키텍처
- BCE 레이어 다이어그램 (Mermaid 또는 ASCII)
- 의존성 방향 설명
- 새 단위 추가 방법 (단계별, 코드 최소화)

## 테스트 실행
mvn test # 또는: gradle test

## 출력 포맷
콘솔 / CSV 각 예시 블록 포함

## 생성형 AI 활용 Activities (6시간)
README의 Activities 섹션 내용을 반영하라.

## 기여 가이드 / ## 라이선스 (MIT)

Markdown만. 표·코드 블록·Mermaid 다이어그램 적극 활용.
```

### Prompt 2. Phase 5 To-Do 리스트 작성 요구

```text
전제: Phase 5 PRD 기능 요구사항·인수 기준 기반
⚠️ 코드 작성 금지. 작업 목록 문서만.

## 🔴 필수 (Must-Have) — v1.0 차단 항목
[ ] 축약된 메서드명 해결 | ST-01 | 단위 테스트 통과
[ ] 중복 키워드 등록 해결 | ST-02 | 중복 키워드 제거 및 테스트 통과
[ ] 비공개 비즈니스 로직 해결 | ST-03 | 불필요 로직 삭제 및 테스트 통과
[ ] 전역 상태 남용 해결 | ST-04 | 테스트 통과
[ ] 의미 없는 Session 해결 | ST-05 | 테스트 통과
[ ] FileHandler 실제 파일 저장로직 없음 해결 | ST-06 | 기능 구현 및 테스트 통과

## 🟡 권장 (Should-Have)
[ ] 입력 검증 (특수문자만 입력, null) | ST-01 | 예외 메시지 일치

## ✅ 완료 항목 / ## 📋 회귀 방지 체크리스트 / ## 🗓️ 마일스톤
```

### Prompt 3. README와 Phase 5 PRD 비교 요구

```text
완성된 README.md와 Phase 5 PRD를 비교하라. 보고만 (수정 금지):
1) PRD에 있으나 README에 빠진 항목
2) README에 있으나 PRD에 없는 항목 (범위 초과 위험)
3) 입출력 계약 불일치 (특히 비율 3.28084 / 1.09361)
4) 테스트 커버리지 목표 불일치
bullet만.
```

### Prompt 4. 세션 보고서와 Prompting 파일 내보내기

```text
이번세션에서 진행한 내용을 report 폴더의 07_README.md & To-Do 리스트 작성_보고서 파일로 내보내주고, 
프롬프트를 포함해서 Prompting 폴더의 07_README.md & To-Do 리스트 작성-Prompting.md 파일로 내보내줘
```

## 3. 프롬프트 해석 기준

| 기준 | 적용 내용 |
|---|---|
| 상위 근거 | Phase 5 PRD 기능 요구사항과 인수 기준 |
| 구현 금지 | 코드 작성, 클래스 수정, 빌드 스크립트 수정 제외 |
| README 목적 | PRD 목적문, 배경, 입력/출력 계약, 아키텍처 원칙, 테스트 실행 방법을 사용자 문서로 정리 |
| To-Do 목적 | v1.0 차단 항목과 권장 항목을 Story ID 및 완료 기준과 함께 추적 |
| 비교 보고 목적 | README와 PRD의 정합성 검토만 수행하고 수정하지 않음 |
| 출력 형식 | Markdown, 표, 코드 블록, Mermaid 다이어그램 사용 |

## 4. 참고한 저장소 자료

| 자료 | 활용 내용 |
|---|---|
| `docs/PRD.md` | Phase 5 목적문, 배경, 기능 요구사항, 입출력 계약, 인수 기준, 테스트 목표 확인 |
| `README.md` | 기존 README 내용 확인 및 최종 README 비교 대상 |
| `pom.xml` | Java 버전과 Maven 기반 실행 환경 확인 |
| `src/main/java/com/example/demo/DemoApplication.java` | 애플리케이션 시작점 확인 |
| `src/main/java/com/example/demo/FeedbackController.java` | 수동 입력, CSV 업로드, 필터링, 다운로드 흐름 확인 |
| `src/main/java/com/example/demo/TextAnalyzer.java` | 감정 및 키워드 분석 방식 확인 |
| `src/main/java/com/example/demo/Constants.java` | 감정 키워드와 카테고리 기준 확인 |
| `src/main/java/com/example/demo/Filters.java` | 감정/키워드 필터링 기준 확인 |
| `docs/analysis.md` | 현재 코드 문제점과 개선 우선순위 확인 |

## 5. 산출물별 작성 기준

### 5.1 README.md

| 요구 | 적용 내용 |
|---|---|
| PRD 1.1 목적문 활용 | README 첫 설명 문장에 반영 |
| 목차 자동 링크 포함 | 주요 섹션 링크 목록 작성 |
| Overview | 해결 문제, OCP/SRP/BCE/TDD 학습 목표, 현재 코드 문제점과 개선 방향 작성 |
| Quick Start | Java/Maven 조건, clone, `mvn spring-boot:run`, 브라우저 접속 주소 작성 |
| 예시 입출력 | `복숭아 좋다` 현재 구현 기준 결과와 `복숭아 최고` PRD 계약 예시 병기 |
| 입력 형식 계약 | 정상 입력 3개, 특수문자-only/null 비정상 입력 작성 |
| 아키텍처 | BCE Mermaid 다이어그램, 의존성 방향, 새 단위 추가 절차 작성 |
| 테스트 실행 | `mvn test`, `gradle test`, PRD 테스트 커버리지 목표 작성 |
| 출력 포맷 | 콘솔, 화면 분석 결과, CSV 예시 작성 |
| Activities | 6시간 활동 흐름으로 정리 |

### 5.2 `docs/PHASE5_TASKS.md`

| 요구 | 적용 내용 |
|---|---|
| Must-Have | 사용자가 제시한 6개 v1.0 차단 항목을 Story ID와 완료 기준으로 정리 |
| Should-Have | 특수문자-only 입력과 null 입력 검증 항목 분리 |
| 완료 항목 | PRD 작성, README 반영, To-Do 문서화 기록 |
| 회귀 방지 체크리스트 | 입력, 분석, 필터링, 출력, 아키텍처 계약으로 분류 |
| 마일스톤 | M1 계약 고정, M2 상태와 책임 정리, M3 CSV 출력 완성, M4 v1.0 릴리스 후보로 정리 |
| 릴리스 게이트 | Must-Have 완료, Should-Have 판단 기록, `mvn test`, PRD 반영 여부 확인 |

### 5.3 README와 PRD 비교 보고

| 비교 항목 | 보고 내용 |
|---|---|
| PRD에 있으나 README에 빠진 항목 | 사용자/이해관계자, 검색 계약, 선택 기능, `parsec:1.0`, 빈 시각화 상태, 필터 결과 없음 정책, Glossary |
| README에 있으나 PRD에 없는 항목 | clone URL, 실행 주소, 이미지, 기여 가이드, 라이선스, Activities, 콘솔 로그 예시, 특수문자-only/null 확장 |
| 입출력 계약 불일치 | `복숭아 최고`와 `복숭아 좋다` 예시 차이, CSV 오류 예시 부족, 검색 계약 누락, `3.28084`/`1.09361` 부재 |
| 테스트 커버리지 목표 불일치 | 핵심 커버리지 수치는 일치하나 입력 계약 100% 문서화 목표는 README 테스트 목표 표에 별도 반영되지 않음 |

## 6. 최종 산출물 구성

| 산출물 | 포함 내용 |
|---|---|
| `README.md` | Phase 5 PRD 기반 사용자/개발자 문서 |
| `docs/PHASE5_TASKS.md` | v1.0 차단 항목, 권장 항목, 완료 항목, 회귀 방지 체크리스트, 마일스톤 |
| `report/07_README.md & To-Do 리스트 작성_보고서.md` | 이번 세션 작업 내용과 산출물 요약 |
| `Prompting/07_README.md & To-Do 리스트 작성-Prompting.md` | 사용자 프롬프트 원문과 해석 기준 |

## 7. 검증 기록

| 항목 | 결과 |
|---|---|
| `README.md` linter 진단 | 오류 없음 |
| `docs/PHASE5_TASKS.md` linter 진단 | 오류 없음 |
| 코드 구현 여부 | 코드 변경 없음 |
| 테스트 실행 | 문서 작업만 수행하여 실행하지 않음 |
