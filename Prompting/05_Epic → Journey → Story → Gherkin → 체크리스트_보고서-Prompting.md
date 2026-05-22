# 05. Epic → Journey → Story → Gherkin → 체크리스트 보고서 Prompting

## 1. 목적

이 문서는 `feedback_analyzer_A02` 저장소를 대상으로 이번 세션에서 작성한 요구사항 산출물의 프롬프트와 결과 구성을 함께 보관하기 위한 Prompting 기록이다.

핵심 목표는 알고리즘 구현이 아니라 계약 정의, 테스트 설계, 레이어 분리 학습이다. 구현 코드는 포함하지 않는다.

## 2. 세션 프롬프트 원문

### Prompt 1. Epic → Journey → Story → Gherkin → 체크리스트 요구

```text
@c:\dev\feedback_analyzer_A02\ 위한 요구사항 서술 패키지. 구현·코드 금지.

Level 1 Epic
  제목: "확장 가능한 Java 단위 변환 학습 시스템"
  목적 4줄, 성공 기준 측정 가능하게 (커버리지, 계약 테스트, 회귀 정책)

Level 2 User Journey (1개 정본)
  Persona: Java/클린 아키텍처 학습자
  단계 5~7: 문제 인식→계약 정의→도메인 분리→Dual-Track TDD→회귀 보호
  각 단계 Pain / Opportunity 1줄

Level 3 User Stories (최소 6개)
  ST-01: 입력 검증 (특수문자, 공백, null 입력 등 예외처리 포함 )
  ST-02: 키워드가 있을경우/없을경우 기반 피드백 분류
  ST-03: 감정 분석 (긍정/부정/중립) 및 존재하지 않는 감정
  ST-04: 피드백 필터링 및 검색
  ST-05: 분석결과 시각화
  ST-06: 결과 CSV 다운로드
  각 Story에 Acceptance Criteria는 체크 가능한 bullet

Level 4 Gherkin Feature 1개
  Background: 고객이 수동 입력 혹은 CSV 입력
  Scenario: happy path — "복숭아 최고" 입력 → 올바른 피드백 출력
  Scenario: 잘못된 형식 — 내용이 없음
  Scenario: unknown unit — "parsec:1.0" 입력
  Scenario: 설정 파일 형식 오류 — 잘못된 JSON/YAML

Level 5 체크리스트
  이 저장소 범위 항목만 / Epic→Journey→Story→Gherkin 정합성 표

Markdown 표와 체크리스트 적극 사용.
```

### Prompt 2. 사용자 여정 스토리보드

```text
@c:\dev\feedback_analyzer_A02\ 사용자 여정을 스토리보드 형식으로.
Stage: Awareness / Entry / Action / Validation / Outcome
각 Stage마다 Action, Thinking, Emotion, Pain, Opportunity 1줄 이상.
목표: 알고리즘이 아닌 계약·테스트·레이어 분리 학습임을 명시. 코드 금지.
```

### Prompt 3. README 기준 Gherkin 8개

```text
README.md 내용을 전제로 Gherkin 8개.
Given-When-Then은 영어 키워드 유지.
반드시 포함:
  - 입력내용에 감정,키워드 관련 단어가 없을 경우
  - 입력내용에 아무것도 입력 안할 경우의 정책
  - CSV 다운로드
  - 키워드 기준으로 감정 분류
구현 금지.
```

### Prompt 4. Level 5 완성도 체크리스트

```text
Epic/Journey/Story/Gherkin과 대조해 Level 5 완성도 체크리스트.
저장소 범위 밖 항목 금지.
각 항목: "누가 무엇을 어떻게 검증하면 통과인지" 한 줄.
표: 영역 / 검증 방법 / 통과 기준 / 추적 ID(Story 번호)
코드 금지.
```

### Prompt 5. 파일 내보내기

```text
이번세션에서 진행한 내용을 report 폴더의 05_Epic → Journey → Story → Gherkin → 체크리스트_보고서 파일로 내보내주고, 
프롬프트를 포함해서 Prompting 폴더의 05_Epic → Journey → Story → Gherkin → 체크리스트_보고서-Prompting.md 파일로 내보내줘
```

## 3. 프롬프트 해석 기준

| 기준 | 적용 내용 |
|---|---|
| 저장소 전제 | `README.md`의 주요 기능을 기준으로 요구사항 범위를 제한한다. |
| 구현 금지 | Java 코드, 테스트 코드, 설정 코드 작성 없이 요구사항 문서만 작성한다. |
| 범위 제한 | 피드백 분석 앱의 실제 기능인 입력, CSV, 분류, 감정 분석, 필터링, 시각화, 다운로드만 포함한다. |
| 학습 목표 | 알고리즘 개선보다 계약, 테스트, 레이어 분리 학습을 우선한다. |
| 추적성 | Epic → Journey → Story → Gherkin → 체크리스트가 서로 연결되도록 ID와 표를 사용한다. |

## 4. 산출물 구성

| 산출물 | 포함 내용 |
|---|---|
| Level 1 Epic | 제목, 목적 4줄, 커버리지/계약 테스트/회귀 정책 기반 성공 기준 |
| Level 2 Journey | 정본 Journey와 Storyboard Journey |
| Level 3 Story | ST-01부터 ST-06까지, 체크 가능한 Acceptance Criteria |
| Level 4 Gherkin | 요구된 Feature 1개와 README 기준 Gherkin 8개 |
| Level 5 체크리스트 | 영역, 검증 방법, 통과 기준, 추적 ID로 구성된 완성도 체크리스트 |
| 정합성 표 | Epic 목적과 Journey, Story, Gherkin 연결 |

## 5. 최종 산출물 요약

### Level 1 Epic

| 항목 | 내용 |
|---|---|
| 제목 | 확장 가능한 Java 단위 변환 학습 시스템 |
| 저장소 적용 범위 | 현재 저장소 기준으로는 `피드백 입력 → 분류/감정 분석 → 필터링/시각화 → CSV 다운로드` 요구사항에 한정한다. |

### Level 2 Journey 핵심 흐름

| 단계 | 핵심 의미 |
|---|---|
| 문제 인식 | 현재 기능과 결함 가능성을 파악한다. |
| 계약 정의 | 입력, 출력, 오류, CSV 형식을 명시한다. |
| 도메인 분리 | 감정 분석, 키워드 분류, 필터링 책임을 나누어 본다. |
| Dual-Track TDD | 계약 테스트와 단위 테스트를 병행한다. |
| 예외 흐름 검증 | 빈 입력, 잘못된 CSV, 미지원 값 등을 점검한다. |
| 회귀 보호 | 기존 시나리오를 자동화해 변경 영향도를 관리한다. |

### Level 3 Stories

| ID | Story |
|---|---|
| ST-01 | 입력 검증 |
| ST-02 | 키워드 유무 기반 피드백 분류 |
| ST-03 | 감정 분석 |
| ST-04 | 피드백 필터링 및 검색 |
| ST-05 | 분석 결과 시각화 |
| ST-06 | 결과 CSV 다운로드 |

### Level 4 Gherkin 8개 시나리오

| 번호 | Scenario |
|---|---|
| 1 | Analyze positive feedback with a keyword |
| 2 | Classify sentiment by keyword |
| 3 | No sentiment or keyword words in input |
| 4 | Empty manual input policy |
| 5 | Upload CSV with required text column |
| 6 | Filter feedback by sentiment and keyword |
| 7 | Visualize analysis results |
| 8 | Download filtered feedback as CSV |

### Level 5 체크리스트 영역

| 영역 | 추적 ID |
|---|---|
| 입력 검증 | ST-01 |
| CSV 업로드 | ST-01 |
| 키워드 분류 | ST-02 |
| 감정 분석 | ST-03 |
| 키워드 기준 감정 분류 | ST-03 |
| 필터링 및 검색 | ST-04 |
| 시각화 | ST-05 |
| CSV 다운로드 | ST-06 |
| 계약 테스트 정합성 | ST-01~ST-06 |
| 회귀 보호 정책 | ST-01~ST-06 |
| 커버리지 정책 | ST-01~ST-06 |

## 6. 생성 파일

| 경로 | 설명 |
|---|---|
| `report/05_Epic → Journey → Story → Gherkin → 체크리스트_보고서.md` | 이번 세션 요구사항 산출물 통합 보고서 |
| `Prompting/05_Epic → Journey → Story → Gherkin → 체크리스트_보고서-Prompting.md` | 프롬프트 원문과 산출물 구성 기록 |
