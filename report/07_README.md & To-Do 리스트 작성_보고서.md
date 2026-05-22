# 07. README.md & To-Do 리스트 작성 보고서

## 1. 보고서 범위

| 항목 | 내용 |
|---|---|
| 대상 저장소 | `feedback_analyzer_A02` |
| 산출물 유형 | README 개편, Phase 5 To-Do 리스트, PRD 비교 보고 |
| 최종 산출물 | `README.md`, `docs/PHASE5_TASKS.md` |
| 기반 문서 | `docs/PRD.md`, 기존 `README.md`, 현재 코드 구조 |
| 핵심 전제 | Phase 5 PRD 기능 요구사항과 인수 기준 기반 |
| 제외 범위 | 코드 구현, 클래스 수정, 빌드 스크립트 수정 |

## 2. 작성 목적

이번 세션의 목적은 Phase 5 PRD를 기준으로 README를 제품 요구사항과 실행 안내가 함께 드러나는 문서로 재구성하고, v1.0 차단 항목과 회귀 방지 체크리스트를 별도 To-Do 문서로 정리하는 것이다.

추가로 완성된 README와 Phase 5 PRD를 비교하여 누락 항목, 범위 초과 위험, 입출력 계약 불일치, 테스트 커버리지 목표 불일치를 보고했다.

## 3. 참조한 근거

### 3.1 Phase 5 PRD 근거

| PRD 섹션 | README 및 To-Do 반영 내용 |
|---|---|
| `1.1 한 줄 목적문` | README 첫 문장에 목적문 반영 |
| `1.2 배경 및 문제 정의` | Overview의 문제 정의와 현재 코드 개선 방향에 반영 |
| `3.1 기능 목록` | 입력 검증, 감정 분석, 키워드 분류, 필터링, 시각화, CSV 다운로드를 README와 To-Do에 반영 |
| `3.2 기능별 입출력 계약` | README 입력 형식 계약, 출력 포맷, To-Do 회귀 방지 체크리스트에 반영 |
| `4.2 아키텍처 원칙` | BCE 레이어 다이어그램과 의존성 방향 설명에 반영 |
| `4.3 테스트 커버리지 목표` | README 테스트 실행 섹션에 커버리지 목표 표로 반영 |
| `7.1 인수 기준` | To-Do Must-Have 추적 메모와 회귀 방지 체크리스트에 반영 |
| `7.2 회귀 보호 규칙` | 기여 가이드와 릴리스 게이트에 반영 |

### 3.2 현재 코드 근거

| 파일 | 확인한 내용 |
|---|---|
| `pom.xml` | 현재 Java 설정이 17이고 Maven 기반임을 확인 |
| `src/main/java/com/example/demo/DemoApplication.java` | Spring Boot 애플리케이션 시작점 확인 |
| `src/main/java/com/example/demo/FeedbackController.java` | 수동 입력, CSV 업로드, 필터링, 다운로드 흐름 확인 |
| `src/main/java/com/example/demo/TextAnalyzer.java` | 감정/키워드 분석 방식 확인 |
| `src/main/java/com/example/demo/Constants.java` | 감정 키워드와 키워드 카테고리 확인 |
| `docs/analysis.md` | 현재 코드의 책임 집중, 전역 상태, 테스트 부족 문제 확인 |

## 4. README.md 작성 결과

README는 다음 구조로 재작성했다.

| 섹션 | 주요 내용 |
|---|---|
| `DemoApplication.java` | PRD 1.1 목적문 기반 한 줄 설명 |
| `목차` | 주요 섹션 자동 링크 목록 |
| `개요 (Overview)` | 해결 문제, 학습 목표, 현재 코드 문제점과 개선 방향 |
| `빠른 시작 (Quick Start)` | Java/Maven 조건, clone, `mvn spring-boot:run`, 예시 입출력 |
| `입력 형식 계약` | 정상 입력 3개, 비정상 입력 2개, CSV 입력 예시 |
| `아키텍처` | BCE Mermaid 다이어그램, 현재 클래스 배치, 의존성 방향, 새 단위 추가 절차 |
| `테스트 실행` | `mvn test`, `gradle test`, PRD 테스트 커버리지 목표 |
| `출력 포맷` | 콘솔, 화면 분석 결과, CSV 다운로드 예시 |
| `생성형 AI 활용 Activities (6시간)` | 프로젝트 파악부터 README 반영까지 6시간 활동 정리 |
| `기여 가이드` | 계약 변경, 실패 테스트, CSV 계약, 빌드 산출물 제외 기준 |
| `라이선스` | MIT License |

## 5. To-Do 리스트 작성 결과

`docs/PHASE5_TASKS.md`를 생성하여 Phase 5 PRD 기반 작업 목록을 정리했다.

### 5.1 Must-Have

| Story | 작업 | 완료 기준 |
|---|---|---|
| ST-01 | 축약된 메서드명 해결 | 단위 테스트 통과 |
| ST-02 | 중복 키워드 등록 해결 | 중복 키워드 제거 및 테스트 통과 |
| ST-03 | 비공개 비즈니스 로직 해결 | 불필요 로직 삭제 및 테스트 통과 |
| ST-04 | 전역 상태 남용 해결 | 테스트 통과 |
| ST-05 | 의미 없는 Session 해결 | 테스트 통과 |
| ST-06 | FileHandler 실제 파일 저장로직 없음 해결 | 기능 구현 및 테스트 통과 |

### 5.2 Should-Have

| Story | 작업 | 완료 기준 |
|---|---|---|
| ST-01 | 입력 검증: 특수문자만 입력 | 예외 메시지 `입력 내용이 필요합니다` 일치 |
| ST-01 | 입력 검증: null 입력 | 예외 메시지 `입력 내용이 필요합니다` 일치 |

### 5.3 추가 구성

| 섹션 | 내용 |
|---|---|
| 완료 항목 | PRD 작성, README 반영, To-Do 문서화 |
| 회귀 방지 체크리스트 | 입력, 분석, 필터링, 출력, 아키텍처 계약 |
| 마일스톤 | M1 계약 고정, M2 상태와 책임 정리, M3 CSV 출력 완성, M4 v1.0 릴리스 후보 |
| 릴리스 게이트 | Must-Have 완료, Should-Have 미완료 판단 기록, `mvn test` 통과, PRD 반영 여부 |

## 6. PRD와 README 비교 보고

### 6.1 PRD에 있으나 README에 빠진 항목

- 타깃 사용자/페르소나/주요 시나리오가 README에 별도 항목으로 없음.
- 기능 목록 중 검색 계약이 README에 거의 반영되지 않음.
- 선택 항목인 잘못된 JSON/YAML 설정 파일 오류 보고가 README에 없음.
- `parsec:1.0` 일반 텍스트 처리 제약이 README에 없음.
- 데이터가 없을 때 시각화 영역의 빈 상태 또는 0값 상태 표시 요구사항이 README에 없음.
- 필터 결과가 없을 때 빈 CSV 제공 또는 다운로드 제한 안내 정책이 README에 없음.
- Glossary 전체가 README에 없음.

### 6.2 README에 있으나 PRD에 없는 항목

- `DemoApplication.java`를 문서 제목으로 삼은 구성은 PRD 제목/범위에는 없음.
- 저장소 clone URL, `http://localhost:8080`, 이미지, 기여 가이드, MIT 라이선스는 PRD 본문 요구사항에는 없음.
- `생성형 AI 활용 Activities (6시간)`은 PRD 본문에는 없음.
- 현재 구현 기준 클래스 배치와 콘솔 로그 예시는 PRD 요구사항보다 구현 설명에 가까움.
- 특수문자-only 입력과 null 입력을 같은 오류 메시지로 확장한 내용은 PRD에 명시되지 않아 범위 초과 위험이 있음.
- CSV escaping 계약은 README에 언급되지만 PRD는 UTF-8, `text` 헤더, 한글 보존까지만 명시함.
- Java 17 기준 설명은 현재 `pom.xml`에는 맞지만 PRD 기술 스택의 Java 21과 다름.

### 6.3 입출력 계약 불일치

- PRD 대표 수동 입력은 `"복숭아 최고"`이고 결과는 `긍정`이지만, README Quick Start 대표 입력은 `"복숭아 좋다"`이고 현재 구현 기준 `중립`으로 설명되어 초점이 다름.
- PRD는 빈 입력/공백-only 입력을 명시하지만, README는 `null`과 특수문자-only까지 같은 오류 메시지로 확장함.
- PRD는 CSV 형식 오류로 `text` 헤더 누락 또는 내용 비어 있음 케이스를 명시하지만, README 비정상 입력 예시는 수동 입력 중심이고 CSV 오류 예시가 부족함.
- PRD는 검색어 문자열 입력과 검색어 포함 피드백 목록 출력을 포함하지만 README 입력/출력 예시에는 검색 계약이 없음.
- `3.28084`와 `1.09361` 비율 값은 README와 Phase 5 PRD 모두에 없음.
- `3.28084`와 `1.09361`이 단위 변환 비율을 의미한다면 Phase 5 PRD의 단위 변환 비목표 및 `parsec:1.0` 일반 텍스트 처리 제약과 충돌 가능성이 큼.

### 6.4 테스트 커버리지 목표 불일치

- Domain 80% 이상, Boundary 계약 테스트 100%, Data 검증 100%, Branch 90% 이상 목표는 README와 PRD가 일치함.
- PRD 목표에는 수동 입력과 CSV 입력의 입력 계약 100% 문서화가 있지만 README 테스트 목표 표에는 이 문서화 목표가 별도 항목으로 없음.
- README는 Gradle 테스트 명령을 추가로 제시하지만 PRD는 Gradle 또는 Maven을 기술 스택으로 허용하므로 직접 불일치는 아님.

## 7. 주요 결정 사항

| 결정 | 내용 |
|---|---|
| 문서 중심 작업 | 사용자 요청에 따라 코드 구현 없이 Markdown 문서만 작성 |
| README 기준 | Phase 5 PRD의 목적문, 배경, 기능 계약, 아키텍처 원칙, 테스트 목표를 기반으로 작성 |
| To-Do 기준 | 사용자가 제공한 Must-Have/Should-Have 항목을 그대로 유지하고 PRD 인수 기준과 연결 |
| 비교 보고 방식 | 수정 없이 누락, 범위 초과, 계약 불일치, 커버리지 차이만 bullet로 보고 |
| 검증 방식 | Markdown 파일 linter 진단 확인 |

## 8. 파일 생성 및 수정 결과

| 경로 | 설명 |
|---|---|
| `README.md` | Phase 5 PRD 기반 README로 수정 |
| `docs/PHASE5_TASKS.md` | v1.0 차단 항목, 권장 항목, 완료 항목, 회귀 방지 체크리스트, 마일스톤 작성 |
| `report/07_README.md & To-Do 리스트 작성_보고서.md` | 이번 세션 보고서 |
| `Prompting/07_README.md & To-Do 리스트 작성-Prompting.md` | 이번 세션 프롬프트 기록 |

## 9. 검증 결과

| 항목 | 결과 |
|---|---|
| `README.md` linter 진단 | 오류 없음 |
| `docs/PHASE5_TASKS.md` linter 진단 | 오류 없음 |
| 코드 구현 여부 | 구현 변경 없음 |
| 테스트 실행 | 문서 작업만 수행하여 `mvn test`는 실행하지 않음 |
