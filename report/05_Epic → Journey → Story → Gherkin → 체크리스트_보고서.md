# 05. Epic → Journey → Story → Gherkin → 체크리스트 보고서

## 1. 보고서 범위

| 항목 | 내용 |
|---|---|
| 대상 저장소 | `feedback_analyzer_A02` |
| 전제 문서 | `README.md` |
| 저장소 기능 범위 | 수동 텍스트 입력, CSV 업로드, 키워드 기반 피드백 분류, 감정 분석, 필터링 및 검색, 분석 결과 시각화, CSV 다운로드 |
| 제외 범위 | 실제 단위 변환 엔진, 저장소에 없는 외부 설정 파일 로더, 구현 코드 작성 |
| 학습 목표 | 알고리즘 구현이 아니라 계약 정의, 테스트 설계, 레이어 분리 학습 |

## 2. Level 1 Epic

| 항목 | 내용 |
|---|---|
| 제목 | 확장 가능한 Java 단위 변환 학습 시스템 |
| 저장소 적용 범위 | 현재 저장소 기준으로는 `피드백 입력 → 분류/감정 분석 → 필터링/시각화 → CSV 다운로드` 요구사항에 한정한다. |

### 목적

- Java/Spring 기반 애플리케이션에서 입력 검증, 분석, 필터링, 출력 책임을 분리해 학습한다.
- 수동 입력과 CSV 입력을 동일한 분석 계약으로 처리한다.
- 키워드 분류와 감정 분석 결과를 사용자에게 검증 가능한 형태로 제공한다.
- 회귀 테스트와 계약 테스트를 통해 기능 확장 시 기존 동작을 보호한다.

### 성공 기준

- [ ] 핵심 도메인 로직의 라인 커버리지 80% 이상, 입력 검증/분류/감정 분석 분기 커버리지 90% 이상.
- [ ] 수동 입력, CSV 입력, 필터링, 다운로드 계약 테스트가 모두 통과한다.
- [ ] 버그 수정 시 실패 재현 테스트를 먼저 추가하고, 이후 동일 결함 재발 시 빌드가 실패한다.
- [ ] 공개 동작 변경은 Story Acceptance Criteria와 Gherkin Scenario 갱신 없이는 병합하지 않는다.

## 3. Level 2 User Journey

### 정본 Journey

| 단계 | 사용자 행동 | Pain | Opportunity |
|---|---|---|---|
| 1. 문제 인식 | 학습자는 피드백 분석 앱의 기능과 결함 가능성을 파악한다. | 입력 형식, 예외, 분석 기준이 암묵적이다. | README와 테스트 요구사항으로 현재 기능 경계를 명확히 한다. |
| 2. 계약 정의 | 입력/출력, 오류 메시지, CSV 형식을 정의한다. | 컨트롤러와 분석 로직이 직접 얽히면 검증이 어렵다. | 수동/CSV 입력을 동일한 피드백 계약으로 정렬한다. |
| 3. 도메인 분리 | 감정 분석, 키워드 분류, 필터링 책임을 분리해 바라본다. | UI 흐름 중심 구현은 도메인 테스트를 어렵게 만든다. | 분석 규칙을 독립적으로 검증 가능한 단위로 만든다. |
| 4. Dual-Track TDD | 계약 테스트와 단위 테스트를 병행한다. | 화면 기준 확인만으로는 회귀를 놓치기 쉽다. | API/컨트롤러 계약과 도메인 규칙을 각각 보호한다. |
| 5. 예외 흐름 검증 | null, 공백, 잘못된 CSV, 미지원 값 등을 점검한다. | 실패 입력이 조용히 무시되거나 일반 오류로 뭉개질 수 있다. | 사용자에게 복구 가능한 오류를 제공한다. |
| 6. 회귀 보호 | 기존 시나리오를 자동화하고 변경 정책을 운영한다. | 키워드 추가나 필터 변경이 기존 결과를 깨뜨릴 수 있다. | 회귀 테스트 세트로 기능 확장을 안전하게 만든다. |

### Storyboard Journey

목표: 이 여정의 핵심은 알고리즘 성능 구현이 아니라, 계약 정의, 테스트 설계, 레이어 분리를 통해 Java/Spring 애플리케이션 구조를 학습하는 것이다.

| Stage | Action | Thinking | Emotion | Pain | Opportunity |
|---|---|---|---|---|---|
| Awareness | 학습자는 피드백 분석 앱의 현재 기능을 살펴보고 입력, 분석, 필터링, 다운로드 흐름을 파악한다. | “이 앱은 어떤 입력을 받고, 어떤 결과를 약속해야 하지?” | 호기심과 약간의 막막함을 느낀다. | 기능은 보이지만 계약, 예외, 책임 경계가 명확하지 않다. | README와 화면 흐름을 기준으로 사용자에게 보장할 동작을 먼저 정리한다. |
| Entry | 학습자는 수동 입력과 CSV 업로드를 대표 진입점으로 선택한다. | “입력 방식은 달라도 분석으로 넘어가는 계약은 같아야 한다.” | 구조를 잡을 수 있다는 기대감이 생긴다. | 수동 입력, CSV 입력, 빈 입력, 잘못된 형식이 섞이면 테스트 기준이 흐려진다. | 입력 계약을 정의해 정상 입력과 실패 입력을 분리하고, 계약 테스트 대상으로 만든다. |
| Action | 학습자는 감정 분석, 키워드 분류, 필터링, 시각화, 다운로드를 독립된 책임으로 나누어 바라본다. | “컨트롤러 동작과 도메인 규칙을 따로 검증해야 유지보수가 쉽다.” | 복잡함이 줄어드는 안정감을 느낀다. | 화면 중심으로만 확인하면 분석 규칙 변경 시 회귀를 놓치기 쉽다. | 레이어별 책임을 분리하고 단위 테스트와 계약 테스트를 병행한다. |
| Validation | 학습자는 빈 입력, 키워드 없음, 존재하지 않는 감정, 잘못된 CSV 같은 실패 흐름을 검증한다. | “성공 케이스보다 실패 케이스가 계약을 더 선명하게 만든다.” | 꼼꼼히 확인해야 한다는 긴장감이 있다. | 오류가 조용히 무시되거나 일반 오류로 뭉개지면 학습 효과가 낮아진다. | 체크 가능한 Acceptance Criteria와 Gherkin 시나리오로 검증 기준을 고정한다. |
| Outcome | 학습자는 테스트로 보호되는 피드백 분석 흐름과 확장 가능한 레이어 구조를 얻는다. | “새 기능은 기존 계약을 깨지 않고 추가되어야 한다.” | 자신감과 통제감을 느낀다. | 테스트 없이 키워드나 필터 정책을 바꾸면 기존 기능이 쉽게 깨질 수 있다. | 회귀 정책을 운영해 변경 시 실패 재현 테스트, 계약 테스트, 커버리지 기준을 함께 유지한다. |

## 4. Level 3 User Stories

| ID | Story | Acceptance Criteria |
|---|---|---|
| ST-01 | 입력 검증 | [ ] null 입력은 분석 대상에 추가되지 않고 검증 오류로 처리된다.<br>[ ] 공백-only 입력은 저장/분석되지 않는다.<br>[ ] 특수문자만 포함된 입력은 정책에 따라 거부 또는 중립 처리 기준이 명시된다.<br>[ ] CSV에서 필수 `text` 컬럼이 없으면 업로드 실패 메시지를 제공한다.<br>[ ] 실패 입력은 기존 분석 결과를 손상시키지 않는다. |
| ST-02 | 키워드 유무 기반 피드백 분류 | [ ] 배송/품질/가격/서비스/사용성 키워드가 있으면 해당 카테고리 카운트가 증가한다.<br>[ ] 키워드가 없으면 어떤 카테고리에도 잘못 포함되지 않는다.<br>[ ] 하나의 피드백에 여러 카테고리 키워드가 있으면 정의된 정책대로 다중 분류 또는 우선순위 분류된다.<br>[ ] 대소문자 차이는 분류 결과에 영향을 주지 않는다. |
| ST-03 | 감정 분석 | [ ] 긍정 키워드가 있으면 긍정으로 집계된다.<br>[ ] 부정 키워드가 있으면 부정으로 집계된다.<br>[ ] 긍정/부정 키워드가 없으면 중립으로 집계된다.<br>[ ] 존재하지 않는 감정 필터 값은 결과 없음 또는 검증 오류로 일관되게 처리된다.<br>[ ] 동일 입력은 반복 분석해도 동일한 감정 결과를 낸다. |
| ST-04 | 피드백 필터링 및 검색 | [ ] 감정 필터 `전체`는 모든 피드백을 유지한다.<br>[ ] 카테고리 필터 `전체`는 모든 카테고리를 허용한다.<br>[ ] 감정+카테고리 조합 필터는 두 조건을 모두 만족하는 피드백만 반환한다.<br>[ ] 검색어가 없으면 필터 결과 전체를 보여준다.<br>[ ] 결과가 없으면 빈 목록과 안내 메시지를 제공한다. |
| ST-05 | 분석 결과 시각화 | [ ] 감정별 집계가 화면에 표시된다.<br>[ ] 카테고리별 집계가 화면에 표시된다.<br>[ ] 입력/업로드 직후 최신 분석 결과가 반영된다.<br>[ ] 필터 적용 후 시각화는 필터링된 데이터 기준으로 갱신된다.<br>[ ] 데이터가 없을 때 차트/요약 영역은 오류 없이 빈 상태를 표시한다. |
| ST-06 | 결과 CSV 다운로드 | [ ] 필터링된 결과를 CSV로 다운로드할 수 있다.<br>[ ] CSV는 UTF-8로 한글이 깨지지 않는다.<br>[ ] 첫 행에는 `text` 헤더가 포함된다.<br>[ ] 필터 결과가 없으면 빈 CSV 또는 다운로드 불가 안내 정책이 명시된다.<br>[ ] 다운로드 데이터는 화면의 필터 결과와 일치한다. |

## 5. Level 4 Gherkin

### Feature 1

```gherkin
Feature: 고객 피드백 분석과 결과 보호

  Background:
    Given 고객이 수동 입력 혹은 CSV 입력으로 피드백을 제공할 수 있다
    And 시스템은 피드백 텍스트를 감정과 키워드 기준으로 분석한다

  Scenario: happy path - "복숭아 최고" 입력 → 올바른 피드백 출력
    Given 고객이 "복숭아 최고"를 수동 입력한다
    When 피드백 분석을 요청한다
    Then 피드백은 저장된다
    And 감정 분석 결과는 "긍정"으로 집계된다
    And 키워드가 없는 카테고리는 증가하지 않는다

  Scenario: 잘못된 형식 - 내용이 없음
    Given 고객이 빈 내용을 입력한다
    When 피드백 분석을 요청한다
    Then 피드백은 저장되지 않는다
    And 사용자에게 입력 내용이 필요하다는 오류가 표시된다
    And 기존 분석 결과는 유지된다

  Scenario: unknown unit - "parsec:1.0" 입력
    Given 고객이 "parsec:1.0"을 입력한다
    When 피드백 분석을 요청한다
    Then 시스템은 이를 일반 텍스트 피드백으로 처리한다
    And 정의된 감정 키워드가 없으면 "중립"으로 집계된다
    And 저장소 범위 밖의 단위 변환 오류로 처리하지 않는다

  Scenario: 설정 파일 형식 오류 - 잘못된 JSON/YAML
    Given 분석 설정 파일이 잘못된 JSON 또는 YAML 형식이다
    When 애플리케이션이 분석 규칙을 로드한다
    Then 시스템은 설정 형식 오류를 보고한다
    And 기본 분석 규칙 사용 여부를 명시된 정책에 따라 결정한다
    And 오류는 회귀 테스트 대상으로 등록된다
```

### README 기준 Gherkin 8개

```gherkin
Feature: Feedback Analyzer requirements based on README

  Background:
    Given the customer can submit feedback by manual text input or CSV upload
    And the system analyzes feedback by keyword category and sentiment
    And the system supports filtering, visualization, and CSV download

  Scenario: Analyze positive feedback with a keyword
    Given the customer enters "배송 최고"
    When the customer requests feedback analysis
    Then the feedback should be accepted
    And the sentiment result should include "긍정"
    And the keyword category result should include "배송"

  Scenario: Classify sentiment by keyword
    Given the customer enters "서비스가 친절하고 만족스러워요"
    When the customer requests feedback analysis
    Then the feedback should be classified as "긍정" based on sentiment keywords
    And the keyword category should include "서비스"

  Scenario: No sentiment or keyword words in input
    Given the customer enters "복숭아를 먹었다"
    When the customer requests feedback analysis
    Then the feedback should be accepted as valid text
    And the sentiment result should be classified as "중립"
    And no keyword category count should increase

  Scenario: Empty manual input policy
    Given the customer enters an empty feedback text
    When the customer requests feedback analysis
    Then the feedback should not be added to the analysis target
    And the system should show a validation message that feedback content is required
    And previous feedback analysis results should remain unchanged

  Scenario: Upload CSV with required text column
    Given the customer uploads a CSV file with a "text" column
    And the CSV contains "가격이 저렴하고 좋아요"
    When the customer requests CSV upload analysis
    Then the feedback rows should be accepted
    And the sentiment result should include "긍정"
    And the keyword category result should include "가격"

  Scenario: Filter feedback by sentiment and keyword
    Given analyzed feedback includes "배송이 빠르고 좋아요"
    And analyzed feedback includes "가격이 비싸서 불만입니다"
    When the customer filters by sentiment "부정" and keyword "가격"
    Then only feedback matching both conditions should be shown
    And "가격이 비싸서 불만입니다" should be included in the filtered result

  Scenario: Visualize analysis results
    Given feedback analysis has completed
    When the customer views the analysis result page
    Then the system should display sentiment summary results
    And the system should display keyword category summary results
    And the visualization should reflect the latest analyzed feedback

  Scenario: Download filtered feedback as CSV
    Given filtered feedback results are available
    When the customer requests CSV download
    Then the system should provide a CSV file
    And the CSV should contain a "text" header
    And the CSV should include the filtered feedback rows
    And Korean text should be downloadable without corruption
```

## 6. Level 5 완성도 체크리스트

저장소 범위: 수동 입력, CSV 업로드, 키워드 기반 분류, 감정 분석, 필터링/검색, 시각화, CSV 다운로드만 포함한다.

| 영역 | 검증 방법 | 통과 기준 | 추적 ID |
|---|---|---|---|
| 입력 검증 - 정상 수동 입력 | 학습자가 수동 입력 화면에서 의미 있는 피드백 문장을 입력하고 분석 결과를 확인한다. | 입력한 문장이 피드백 목록에 추가되고 감정/키워드 분석 대상에 포함되면 통과. | ST-01 |
| 입력 검증 - 빈 입력 | 학습자가 아무 내용도 입력하지 않은 상태로 분석을 요청한다. | 빈 피드백이 저장되지 않고 기존 분석 결과가 유지되며 입력 필요 정책이 확인되면 통과. | ST-01 |
| 입력 검증 - 공백 입력 | 학습자가 공백만 포함된 값을 입력하고 분석을 요청한다. | 공백 입력이 저장되지 않고 분석 카운트가 증가하지 않으면 통과. | ST-01 |
| 입력 검증 - 특수문자 입력 | 학습자가 특수문자만 포함된 피드백을 입력하고 분석 결과를 확인한다. | 저장 여부와 감정 처리 방식이 정의된 정책대로 일관되게 적용되면 통과. | ST-01 |
| CSV 업로드 - 필수 컬럼 | 학습자가 `text` 컬럼이 포함된 CSV를 업로드한다. | CSV의 각 피드백 행이 분석 대상에 포함되고 전체 입력 수가 반영되면 통과. | ST-01 |
| CSV 업로드 - 잘못된 형식 | 학습자가 `text` 컬럼이 없거나 내용이 비어 있는 CSV를 업로드한다. | 오류 안내가 표시되고 기존 피드백 데이터가 손상되지 않으면 통과. | ST-01 |
| 키워드 분류 - 키워드 있음 | 학습자가 배송, 품질, 가격, 서비스, 사용성 중 하나의 키워드가 포함된 문장을 입력한다. | 해당 카테고리 집계가 증가하고 다른 카테고리는 정책에 맞게 유지되면 통과. | ST-02 |
| 키워드 분류 - 키워드 없음 | 학습자가 감정/키워드 관련 단어가 없는 일반 문장을 입력한다. | 피드백은 유효하게 처리되지만 키워드 카테고리 집계가 증가하지 않으면 통과. | ST-02 |
| 키워드 분류 - 복수 키워드 | 학습자가 둘 이상의 카테고리 키워드가 포함된 문장을 입력한다. | 정의된 다중 분류 정책에 따라 관련 카테고리 결과가 일관되게 반영되면 통과. | ST-02 |
| 감정 분석 - 긍정 | 학습자가 긍정 키워드가 포함된 피드백을 입력한다. | 감정 결과에서 긍정 카운트가 증가하면 통과. | ST-03 |
| 감정 분석 - 부정 | 학습자가 부정 키워드가 포함된 피드백을 입력한다. | 감정 결과에서 부정 카운트가 증가하면 통과. | ST-03 |
| 감정 분석 - 중립 | 학습자가 긍정/부정 키워드가 없는 피드백을 입력한다. | 감정 결과에서 중립 카운트가 증가하면 통과. | ST-03 |
| 감정 분석 - 존재하지 않는 감정 필터 | 학습자가 지원하지 않는 감정 값을 필터 조건으로 사용한다. | 정의된 정책에 따라 결과 없음 또는 검증 오류가 일관되게 표시되면 통과. | ST-03 |
| 키워드 기준 감정 분류 | 학습자가 “최고”, “불만”, “보통”처럼 감정 키워드가 포함된 피드백을 입력한다. | 감정 키워드 기준으로 긍정/부정/중립 결과가 기대값과 일치하면 통과. | ST-03 |
| 필터링 - 감정 전체 | 학습자가 감정 필터를 `전체`로 선택한다. | 감정 조건 때문에 제외되는 피드백이 없으면 통과. | ST-04 |
| 필터링 - 키워드 전체 | 학습자가 키워드 필터를 `전체`로 선택한다. | 키워드 조건 때문에 제외되는 피드백이 없으면 통과. | ST-04 |
| 필터링 - 감정과 키워드 조합 | 학습자가 특정 감정과 특정 키워드 카테고리를 함께 선택한다. | 두 조건을 모두 만족하는 피드백만 화면에 표시되면 통과. | ST-04 |
| 필터링 - 결과 없음 | 학습자가 일치 항목이 없는 필터 조합을 선택한다. | 빈 결과와 결과 없음 안내가 표시되고 오류가 발생하지 않으면 통과. | ST-04 |
| 검색 - 검색어 있음 | 학습자가 특정 단어를 검색 조건으로 입력한다. | 해당 단어를 포함한 피드백만 결과에 남으면 통과. | ST-04 |
| 검색 - 검색어 없음 | 학습자가 검색어 없이 필터 결과를 확인한다. | 검색 조건 때문에 추가 제외가 발생하지 않으면 통과. | ST-04 |
| 시각화 - 초기 빈 상태 | 학습자가 피드백이 없는 상태에서 결과 영역을 확인한다. | 감정/키워드 시각화 영역이 오류 없이 빈 상태 또는 0값 상태로 표시되면 통과. | ST-05 |
| 시각화 - 분석 직후 | 학습자가 피드백 분석 직후 화면의 요약/차트를 확인한다. | 감정별 및 키워드별 집계가 최신 입력을 반영하면 통과. | ST-05 |
| 시각화 - 필터 적용 후 | 학습자가 필터를 적용한 뒤 결과 시각화를 확인한다. | 시각화가 전체 데이터가 아닌 필터링된 결과 기준으로 갱신되면 통과. | ST-05 |
| CSV 다운로드 - 필터 결과 있음 | 학습자가 필터링된 결과가 있는 상태에서 다운로드를 요청한다. | 다운로드된 CSV에 `text` 헤더와 화면의 필터 결과가 포함되면 통과. | ST-06 |
| CSV 다운로드 - 한글 인코딩 | 학습자가 한글 피드백이 포함된 결과를 CSV로 다운로드해 확인한다. | CSV 파일에서 한글 텍스트가 깨지지 않으면 통과. | ST-06 |
| CSV 다운로드 - 결과 없음 | 학습자가 필터 결과가 없는 상태에서 다운로드 정책을 확인한다. | 빈 CSV 제공 또는 다운로드 제한 안내가 명시된 정책대로 동작하면 통과. | ST-06 |
| 계약 테스트 정합성 | 학습자가 Epic, Journey, Story, Gherkin의 동일 기능 항목을 서로 대조한다. | 각 Story가 최소 하나 이상의 Journey 단계와 Gherkin 시나리오로 추적되면 통과. | ST-01~ST-06 |
| 회귀 보호 정책 | 학습자가 결함 수정 전 실패 재현 테스트 존재 여부를 확인한다. | 수정된 결함마다 재현 테스트가 있고 동일 결함 재발 시 테스트가 실패하면 통과. | ST-01~ST-06 |
| 커버리지 정책 | 학습자가 테스트 리포트에서 입력 검증, 분석, 필터링, 다운로드 범위를 확인한다. | 핵심 도메인 로직 80% 이상, 주요 분기 90% 이상 기준을 만족하면 통과. | ST-01~ST-06 |

## 7. Epic → Journey → Story → Gherkin 정합성 표

| Epic 목적 | Journey 단계 | Story | Gherkin 연결 |
|---|---|---|---|
| 입력 계약을 명확히 한다. | Entry, Validation | ST-01 | Empty manual input policy, Upload CSV with required text column |
| 키워드 분류 규칙을 검증 가능하게 한다. | Action, Validation | ST-02 | Analyze positive feedback with a keyword, No sentiment or keyword words in input |
| 감정 분석 결과를 계약으로 보호한다. | Action, Validation | ST-03 | Classify sentiment by keyword, No sentiment or keyword words in input |
| 결과 탐색과 사용 흐름을 보장한다. | Action, Outcome | ST-04, ST-05 | Filter feedback by sentiment and keyword, Visualize analysis results |
| 분석 결과를 외부 산출물로 제공한다. | Outcome | ST-06 | Download filtered feedback as CSV |
| 회귀를 방지한다. | Dual-Track TDD, 회귀 보호 | ST-01~ST-06 | 전체 시나리오 |
