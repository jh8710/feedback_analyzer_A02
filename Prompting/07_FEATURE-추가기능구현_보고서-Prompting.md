# 07_FEATURE-추가기능구현 보고서 - Prompting

## 1. 목적

이 문서는 이번 세션에서 사용한 사용자 요청과 작업 프롬프트 흐름을 기록한다.

작업 목표:

- `test_feedback_trend.csv` 기반 Trend 시각화 추가
- 감정 분석 및 감정 필터 키워드를 File DB로 관리하도록 변경
- 기능 구현 과정의 코드 확인, 구현, 검증 흐름 기록
- 사용자 프롬프트와 최종 산출물 정리

## 2. 사용자 요청 프롬프트

### 2.1 추가 기능 구현 요청

```text
1. test_feedback_trend.csv로 Trend 시각화 추가
2. 감정 분석 필터를 File DB로 관리 하도록 변경
```

의도:

- 날짜별 피드백 감정 변화 추이를 화면에서 볼 수 있도록 구현
- 감정 분석 및 필터링에 사용하는 키워드를 코드가 아닌 파일 기반 데이터로 관리
- 향후 키워드 변경 시 Java 코드를 수정하지 않아도 되도록 구조 개선

### 2.2 보고서 및 프롬프트 기록 요청

```text
이번세션에서 진행한 내용을 report 폴더의 07_FEATURE-추가기능구현_보고서 파일로 내보내주고, 
프롬프트를 포함해서 Prompting 폴더의 07_FEATURE-추가기능구현_보고서-Prompting.md 파일로 내보내줘
```

의도:

- 이번 세션의 기능 구현 내용을 보고서로 남김
- 사용자 프롬프트와 작업 흐름을 별도 Prompting 문서로 보관
- `report`와 `Prompting` 폴더에 07번 산출물 생성

## 3. 코드 확인 단계 프롬프트 구조

구현 전 다음 관점으로 코드베이스를 확인했다.

```text
Inspect the current feedback analyzer structure.

Read:
- src/main/java/com/example/demo/FeedbackController.java
- src/main/java/com/example/demo/FeedbackService.java
- src/main/java/com/example/demo/TextAnalyzer.java
- src/main/java/com/example/demo/Filters.java
- src/main/java/com/example/demo/Constants.java
- src/main/java/com/example/demo/UIComponents.java
- src/main/resources/templates/index.html
- src/test/java/com/example/demo/DemoApplicationTests.java
- pom.xml

Check:
1. Where the view model is assembled.
2. Where sentiment analysis keywords are defined.
3. Whether sentiment filtering uses the same rules as sentiment analysis.
4. Where a Trend dataset can be loaded and passed to the template.
5. Whether OpenCSV is already available.
6. Which tests already cover sentiment and filtering behavior.
```

확인 결과:

- `FeedbackController`는 공통 모델 속성을 `addCommonModelAttributes()`에서 추가하고 있었다.
- `FeedbackService`는 피드백 입력, 업로드, 필터링, 분석 조합을 담당하고 있었다.
- `TextAnalyzer`는 감정 분석 시 `Constants.SENTIMENT_KEYWORDS`를 사용했다.
- `Filters`는 감정 필터링 시 내부 static Map인 `S_KEYWORDS`를 별도로 사용했다.
- 감정 분석과 감정 필터링의 키워드 출처가 달라 통합 여지가 있었다.
- `index.html`에는 감정 분포와 키워드 분포는 있었지만 Trend 영역은 없었다.
- `pom.xml`에는 OpenCSV 의존성이 이미 있어 CSV File DB 구현에 재사용할 수 있었다.

## 4. 관련 요구사항 확인 프롬프트 구조

Trend와 File DB 요구가 프로젝트 문서에 언급되어 있는지 확인하기 위해 다음 검색 관점을 적용했다.

```text
Search the repository for:
- test_feedback_trend
- trend
- 감정
- sentiment
- filter

Focus on:
1. Whether the Trend CSV file already exists.
2. Whether the expected Trend feature is described in project notes.
3. Whether File DB is mentioned as a refactoring or feature direction.
4. Whether there are existing report conventions to follow.
```

확인 결과:

- `project_purpose.md`에 `Trend 를 볼 수 있도록 시각화 (test_feedback_trend.csv 파일 사용)`이 언급되어 있었다.
- 같은 문서에 `감정분석 필터의 관리를 위한 DB화(File DB 사용)`이 언급되어 있었다.
- 저장소에는 기존 `test_feedback_trend.csv` 파일이 없었다.
- 기존 보고서는 `report`와 `Prompting` 폴더에 번호별 Markdown 파일로 관리되고 있었다.

## 5. 구현 계획 프롬프트 구조

구현 전 다음 기준으로 변경 범위를 정했다.

```text
Implement the requested features with minimal disruption.

For sentiment File DB:
- Add a CSV-based file database for sentiment keywords.
- Prefer the external data/ file when it exists.
- Fall back to src/main/resources/data/ for packaged runtime.
- Keep a default fallback from existing constants.
- Make TextAnalyzer and Filters use the same sentiment detector.
- Preserve neutral-over-positive priority from existing filter bug fix.

For Trend visualization:
- Add a CSV-based file database for trend data.
- Use test_feedback_trend.csv with date, positive, neutral, negative columns.
- Expose trend points through FeedbackService.
- Add trendPoints to common controller model attributes.
- Render a simple visual trend in index.html without adding new dependencies.

For tests:
- Add unit tests for sentiment keyword CSV loading.
- Add unit tests for trend CSV loading.
- Run mvn test.
```

판단:

- 새 라이브러리를 추가하지 않고 기존 OpenCSV를 사용한다.
- Trend 시각화는 현재 템플릿 구조에 맞춰 CSS 기반 누적 막대로 구현한다.
- 감정 키워드는 외부 `data/` 파일을 우선 사용하고, 리소스 파일을 fallback으로 둔다.
- 기존 public method 이름은 유지해 변경 범위를 줄인다.

## 6. 구현 프롬프트 구조

수정 단계에서는 다음 기준을 적용했다.

```text
Create SentimentKeywordFileDb.

Requirements:
- Read CSV rows with sentiment and keyword columns.
- Expose sentiment labels.
- Expose keywords by sentiment.
- Detect sentiment from text.
- Use priority: neutral, negative, positive.
- Return neutral when no keyword matches.
- Support external file path from application.properties.
- Support classpath fallback.
- Support default keyword fallback.
```

핵심 구현:

- `SentimentKeywordFileDb` 클래스 추가
- `feedback.sentiment-keywords.path` 설정 추가
- `data/sentiment_keywords.csv` 추가
- `src/main/resources/data/sentiment_keywords.csv` 추가
- `TextAnalyzer`에서 감정 라벨과 감정 판정을 File DB에 위임
- `Filters`에서 감정 필터 판정을 File DB에 위임

```text
Create FeedbackTrendFileDb.

Requirements:
- Read CSV rows with date, positive, neutral, negative columns.
- Expose TrendPoint objects.
- Provide total count for visualization.
- Support external file path from application.properties.
- Support classpath fallback.
```

핵심 구현:

- `FeedbackTrendFileDb` 클래스 추가
- `FeedbackTrendFileDb.TrendPoint` 내부 클래스 추가
- `feedback.trend.path` 설정 추가
- `data/test_feedback_trend.csv` 추가
- `src/main/resources/data/test_feedback_trend.csv` 추가
- `FeedbackService.getTrendPoints()` 추가
- `FeedbackController.addCommonModelAttributes()`에 `trendPoints` 추가

```text
Update index.html to render Trend.

Requirements:
- Show a Trend section when trendPoints exists.
- Render one row per date.
- Use stacked bars for positive, neutral, negative ratios.
- Show total count per date.
- Keep existing page layout and styling style.
```

핵심 구현:

- Trend 영역 CSS 추가
- 색상 범례 추가
- 날짜별 누적 막대 추가
- `point.total`, `point.positive`, `point.neutral`, `point.negative` 기반 비율 계산

## 7. 검증 프롬프트 구조

수정 후 다음 검증을 수행했다.

```text
Verify the feature implementation.

Check:
1. IDE diagnostics for edited Java files and tests.
2. No compile errors from new Spring services and constructors.
3. TextAnalyzer and Filters still pass existing sentiment/filter tests.
4. New CSV File DB tests pass.
5. Thymeleaf template syntax is accepted by the test context.
6. Run mvn test.
7. Review git diff and changed file list.
```

검증 결과:

- `ReadLints`: 편집 파일 linter 오류 없음
- `mvn test`: 성공
- Tests run: 14
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

참고:

- Maven 컴파일 출력에 `Session.java uses unchecked or unsafe operations` 경고가 표시되었으나 이번 작업 범위 밖의 기존 경고로 판단했다.
- 테스트 실행으로 `target/` 빌드 산출물이 갱신되었다.

## 8. 테스트 추가 프롬프트 구조

테스트 추가 시 다음 기준을 적용했다.

```text
Add focused tests for new file-backed behavior.

For SentimentKeywordFileDb:
- Create a temporary CSV file.
- Include one keyword for positive, neutral, and negative.
- Instantiate the File DB with the temporary path.
- Assert sentiment detection uses file content.

For FeedbackTrendFileDb:
- Create a temporary CSV file.
- Include two date rows.
- Instantiate the File DB with the temporary path.
- Assert row count and parsed values.
- Assert total count is computed correctly.
```

추가된 테스트:

- `sentimentKeywordFileDbLoadsKeywordsFromCsvFile`
- `feedbackTrendFileDbLoadsTrendPointsFromCsvFile`

## 9. 기능 평가 프롬프트 구조

최종 판단은 다음 기준으로 정리했다.

```text
Evaluate whether the user's two feature requests are complete.

Criteria:
- test_feedback_trend.csv exists.
- Trend data is loaded from CSV.
- Trend data is visible in the main UI.
- Sentiment keywords are stored in a file database.
- TextAnalyzer uses the file-backed sentiment rules.
- Filters uses the same file-backed sentiment rules.
- Tests cover both new file-backed readers.
- Existing behavior remains covered by previous tests.
```

판단:

- `test_feedback_trend.csv` 기반 Trend 시각화가 추가되었다.
- 감정 키워드가 `sentiment_keywords.csv` File DB로 이동했다.
- 감정 분석과 감정 필터링이 같은 File DB를 사용한다.
- 기존 감정/필터 테스트와 신규 CSV 로딩 테스트가 모두 통과했다.

## 10. 보고서 작성용 프롬프트 구조

이번 보고서 작성 시 사용한 정리 기준은 다음과 같다.

```text
Export this session's feature implementation as a report.

Include:
- session overview
- referenced files
- previous state
- changes for sentiment keyword File DB
- changes for Trend CSV loading
- UI visualization details
- test additions
- test execution result
- linter result
- requirements satisfaction
- remaining risks and follow-up work

Also create a separate prompting document that includes:
- the user's prompts
- investigation prompt structure
- implementation prompt structure
- verification prompt structure
- testing prompt structure
- final evaluation prompt structure
```

## 11. 작업 결과 요약

생성 및 수정된 주요 파일:

- `src/main/java/com/example/demo/SentimentKeywordFileDb.java`
- `src/main/java/com/example/demo/FeedbackTrendFileDb.java`
- `src/main/java/com/example/demo/TextAnalyzer.java`
- `src/main/java/com/example/demo/Filters.java`
- `src/main/java/com/example/demo/FeedbackService.java`
- `src/main/java/com/example/demo/FeedbackController.java`
- `src/main/resources/templates/index.html`
- `src/main/resources/application.properties`
- `data/sentiment_keywords.csv`
- `data/test_feedback_trend.csv`
- `src/main/resources/data/sentiment_keywords.csv`
- `src/main/resources/data/test_feedback_trend.csv`
- `src/test/java/com/example/demo/DemoApplicationTests.java`
- `report/07_FEATURE-추가기능구현_보고서.md`
- `Prompting/07_FEATURE-추가기능구현_보고서-Prompting.md`

핵심 판단:

- 이번 작업은 요청된 Trend 시각화와 감정 분석 필터 File DB 관리를 충족한다.
- 테스트 결과는 `Tests run: 14, Failures: 0, Errors: 0, Skipped: 0`이다.
- 다음 단계에서는 카테고리 키워드도 File DB로 통합하고, Trend 시각화 검증을 MVC 테스트로 보강하는 것을 권장한다.
