# 리팩토링 전후 비교 보고서

## 1. 개요

이번 리팩토링은 Java 17 Spring Boot 기반 피드백 분석기의 구조를 개선하고, 기존 기능을 보존하면서 테스트 가능성과 유지보수성을 높이는 것을 목표로 진행되었다.

핵심 개선 방향은 다음과 같다.

- `FeedbackController`에 몰려 있던 비즈니스 로직을 `FeedbackService`로 이동
- 감정 분석, 카테고리 분석, 필터링 로직에 대한 단위 테스트 보강
- 축약된 메서드명과 변수명을 도메인 중심 이름으로 변경
- 반복되던 정규화, 키워드 매칭, 카운트 증가 로직을 private 메서드로 추출
- 감정 키워드를 File DB CSV로 관리하도록 변경
- `test_feedback_trend.csv` 기반 Trend 시각화 기능 추가

기존 소스의 상세한 이전 버전은 현재 Git diff에 남아 있지 않으므로, Before 코드는 기존 분석/리팩토링 보고서에 기록된 구조와 코드 스멜을 기준으로 대표 형태를 정리했다. After 코드는 현재 코드베이스의 구현을 기준으로 작성했다.

## 2. 테스트 구조 개선

### Before

기존 테스트는 Spring 컨텍스트가 로딩되는지만 확인하는 수준이었다. 감정 분석, 카테고리 분석, 필터링, 파일 처리 동작에 대한 직접 검증이 없었다.

```java
@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

이 구조의 문제는 다음과 같았다.

- 실제 비즈니스 로직이 동작하는지 확인하지 못함
- 필터링 조건별 회귀 버그를 잡기 어려움
- Spring 컨텍스트 초기화 실패가 단위 로직 테스트까지 막을 수 있음

### After

현재 테스트는 대상 클래스를 직접 생성해 핵심 로직을 단위 테스트한다.

```java
@Test
void textAnalyzerCountsSentimentsByKeyword() {
    TextAnalyzer analyzer = new TextAnalyzer();
    List<Feedback> feedbacks = List.of(
            new Feedback("배송이 빠르고 제품이 좋아요"),
            new Feedback("품질문제로 환불하고 싶습니다"),
            new Feedback("포장은 보통입니다")
    );

    Map<String, Integer> result = analyzer.analyzeSentiments(feedbacks);

    assertEquals(1, result.get("긍정"));
    assertEquals(1, result.get("부정"));
    assertEquals(1, result.get("중립"));
}
```

```java
@Test
void filtersBySentimentAndCategoryTogether() {
    Filters filters = new Filters();
    List<Feedback> feedbacks = List.of(
            new Feedback("택배가 늦고 불만입니다"),
            new Feedback("택배가 좋아요"),
            new Feedback("가격이 비싸서 불만입니다")
    );

    List<Feedback> result = filters.fil(feedbacks, "부정", "배송");

    assertEquals(1, result.size());
    assertEquals("택배가 늦고 불만입니다", result.get(0).getText());
}
```

개선 효과:

- 감정 분석, 카테고리 분석, 필터링 조건을 직접 검증
- `중립` 필터 버그처럼 재발 가능성이 높은 케이스를 회귀 테스트로 고정
- Spring 컨텍스트에 의존하지 않는 빠른 단위 테스트 구조 확보

## 3. 네이밍과 매직 값 개선

### Before

기존 분석 코드는 축약된 이름과 문자열 리터럴이 많아 의미를 파악하기 어려웠다.

```java
public Map<String, Integer> sent(List<Feedback> data) {
    Map<String, Integer> res = new HashMap<>();
    res.put("긍정", 0);
    res.put("중립", 0);
    res.put("부정", 0);

    for (Feedback f : data) {
        String txt = f.getText().toLowerCase();
        // keyword matching...
    }

    return res;
}
```

문제점:

- `sent`, `kw`, `res`, `txt` 같은 이름만으로 역할을 알기 어려움
- `"긍정"`, `"중립"`, `"부정"`, `"main"`, `"sub"` 같은 문자열이 코드에 직접 반복됨
- 초기 카운트 `0`의 의미가 코드에 드러나지 않음

### After

도메인 문자열과 맵 구조 키를 상수화하고, 공개 메서드명을 분석 목적에 맞게 바꾸었다.

```java
public class Constants {
    public static final String SENTIMENT_POSITIVE = "긍정";
    public static final String SENTIMENT_NEUTRAL = "중립";
    public static final String SENTIMENT_NEGATIVE = "부정";

    public static final String CATEGORY_DELIVERY = "배송";
    public static final String CATEGORY_QUALITY = "품질";
    public static final String CATEGORY_PRICE = "가격";
    public static final String CATEGORY_SERVICE = "서비스";
    public static final String CATEGORY_USABILITY = "사용성";

    public static final String CATEGORY_MAIN_KEY = "main";
    public static final String CATEGORY_SUB_KEY = "sub";

    public static final int INITIAL_ANALYSIS_COUNT = 0;
}
```

```java
public Map<String, Integer> analyzeSentiments(List<Feedback> feedbacks) {
    Map<String, Integer> sentimentCounts = initializeCounts(sentimentKeywordFileDb.getSentimentLabels());

    for (Feedback feedback : feedbacks) {
        incrementCount(sentimentCounts, detectSentiment(normalize(feedback)));
    }

    latestSentimentCounts = sentimentCounts;
    return sentimentCounts;
}
```

개선 효과:

- 분석 도메인의 핵심 용어가 코드에 명확히 드러남
- 오타로 인한 분기 오류 가능성 감소
- 테스트와 호출부도 새 메서드명 기준으로 읽기 쉬워짐

## 4. 긴 함수와 중복 로직 제거

### Before

감정 분석과 카테고리 분석 메서드 안에 텍스트 정규화, 키워드 포함 여부 판단, 카운트 증가 로직이 반복되어 있었다.

```java
for (Feedback feedback : feedbacks) {
    String text = feedback.getText().toLowerCase();

    if (positiveKeywords.stream().anyMatch(keyword -> text.contains(keyword))) {
        result.put("긍정", result.get("긍정") + 1);
    } else if (negativeKeywords.stream().anyMatch(keyword -> text.contains(keyword))) {
        result.put("부정", result.get("부정") + 1);
    } else {
        result.put("중립", result.get("중립") + 1);
    }
}
```

문제점:

- 분석 흐름과 세부 판정 로직이 한 메서드에 섞임
- 키워드 매칭 방식이 여러 위치에 중복됨
- 카운트 증가 방식이 직접 노출되어 실수 가능성이 높음

### After

분석 메서드는 흐름만 표현하고, 세부 책임은 이름 있는 private 메서드로 분리했다.

```java
private Map<String, Integer> initializeCounts(Collection<String> labels) {
    Map<String, Integer> counts = new HashMap<>();
    for (String label : labels) {
        counts.put(label, Constants.INITIAL_ANALYSIS_COUNT);
    }
    return counts;
}

private String normalize(Feedback feedback) {
    return feedback.getText().toLowerCase();
}

private String detectSentiment(String normalizedFeedbackText) {
    return sentimentKeywordFileDb.detectSentiment(normalizedFeedbackText);
}

private boolean containsAnyKeyword(String normalizedFeedbackText, List<String> keywords) {
    return keywords.stream().anyMatch(normalizedFeedbackText::contains);
}

private void incrementCount(Map<String, Integer> counts, String label) {
    counts.put(label, counts.get(label) + 1);
}
```

개선 효과:

- public 메서드에서 전체 분석 흐름을 쉽게 읽을 수 있음
- 텍스트 정규화, 키워드 매칭, 카운트 증가 정책을 한 곳에서 관리
- 향후 `SentimentAnalyzer`, `CategoryAnalyzer`, `KeywordMatcher` 등으로 분리하기 쉬운 구조가 됨

## 5. Controller 책임 분리

### Before

기존 `FeedbackController`는 HTTP 요청 처리 외에도 세션 조회, 피드백 추가, CSV 파싱, 분석 실행, 필터링 결과 저장까지 직접 담당했다.

```java
@PostMapping("/analyze")
public String analyze(@RequestParam("text") String text, Model model) {
    List<Feedback> feedbacks = Session.getCurrentFeedbacks();

    if (text != null && !text.trim().isEmpty()) {
        feedbacks.add(new Feedback(text.trim()));
    }

    Session.updateInternalData("current_feedbacks", feedbacks);

    Map<String, Integer> sentimentResults = textAnalyzer.analyzeSentiments(feedbacks);
    Map<String, Integer> keywordResults = textAnalyzer.analyzeCategoryKeywords(feedbacks);

    model.addAttribute("feedbacks", feedbacks);
    model.addAttribute("sentimentResults", sentimentResults);
    model.addAttribute("keywordResults", keywordResults);

    return "index";
}
```

문제점:

- Controller가 HTTP 계층과 비즈니스 처리 책임을 동시에 가짐
- `Session`, `TextAnalyzer`, `Filters`, CSV 파싱 세부 구현이 Controller에 노출됨
- 필터링 결과 상태가 Controller 필드에 있어 다운로드 책임과 섞임

### After

Controller는 요청 파라미터를 받고 Service 결과를 Model에 담는 역할에 집중한다.

```java
@PostMapping("/analyze")
public String analyze(@RequestParam("text") String text, Model model) {
    try {
        FeedbackService.AnalysisResult result = feedbackService.addAndAnalyze(text);

        model.addAttribute("success", result.getFeedbacks().size() + "개의 피드백이 입력되었습니다.");
        model.addAttribute("feedbacks", result.getFeedbacks());
        if (result.hasResults()) {
            model.addAttribute("sentimentResults", result.getSentimentResults());
            model.addAttribute("keywordResults", result.getKeywordResults());
        }

    } catch (Exception e) {
        logger.logError("오류 발생: %s", e.getMessage());
        model.addAttribute("error", "처리 중 오류가 발생했습니다.");
    }

    addCommonModelAttributes(model);
    return "index";
}
```

비즈니스 흐름은 `FeedbackService`로 이동했다.

```java
public AnalysisResult addAndAnalyze(String text) {
    List<Feedback> feedbacks = Session.getCurrentFeedbacks();

    if (text != null && !text.trim().isEmpty()) {
        feedbacks.add(new Feedback(text.trim()));
    }

    Session.updateInternalData("current_feedbacks", feedbacks);

    if (feedbacks.isEmpty()) {
        return AnalysisResult.empty(feedbacks);
    }

    Map<String, Integer> sentimentResults = textAnalyzer.analyzeSentiments(feedbacks);
    Map<String, Integer> keywordResults = textAnalyzer.analyzeCategoryKeywords(feedbacks);

    return new AnalysisResult(feedbacks, sentimentResults, keywordResults);
}
```

개선 효과:

- Controller는 요청/응답과 View Model 구성에 집중
- 피드백 추가, 업로드, 분석, 필터링 흐름은 Service에서 관리
- 향후 Controller MVC 테스트와 Service 단위 테스트를 분리하기 쉬워짐

## 6. CSV 업로드 처리 개선

### Before

기존 업로드 처리는 Controller에서 업로드 파일을 임시 경로에 저장한 뒤 다시 읽는 형태였다.

```java
File dest = new File("C:\\tmp\\" + file.getOriginalFilename());
file.transferTo(dest);

try (CSVReader csvReader = new CSVReader(new FileReader(dest))) {
    String[] line;
    csvReader.readNext();
    while ((line = csvReader.readNext()) != null) {
        feedbacks.add(new Feedback(line[0]));
    }
}
```

문제점:

- OS별 경로에 의존
- 불필요한 임시 파일 생성
- Controller가 파일 저장과 CSV 파싱 책임까지 가짐

### After

현재는 `MultipartFile.getInputStream()`을 직접 읽어 Service에서 처리한다.

```java
public List<Feedback> uploadFeedbacks(MultipartFile file) {
    if (file.isEmpty()) {
        return Session.getCurrentFeedbacks();
    }

    List<Feedback> feedbacks = Session.getCurrentFeedbacks();

    try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
        String[] line;
        csvReader.readNext();
        while ((line = csvReader.readNext()) != null) {
            feedbacks.add(new Feedback(line[0]));
        }
    } catch (Exception e) {
        logger.logError("csv 파일 처리 오류: %s", e.getMessage());
        throw new RuntimeException(e);
    }

    Session.updateInternalData("current_feedbacks", feedbacks);
    return feedbacks;
}
```

개선 효과:

- OS 의존 경로 제거
- 임시 파일 생성 없이 업로드 스트림을 바로 처리
- CSV 처리 책임을 Controller 밖으로 이동

## 7. 감정 키워드 File DB화

### Before

감정 키워드가 여러 위치에 분산되어 있었다.

```java
public static final Map<String, List<String>> SENTIMENT_KEYWORDS = new HashMap<>();

static {
    SENTIMENT_KEYWORDS.put("긍정", Arrays.asList("좋아요", "만족", "감사"));
    SENTIMENT_KEYWORDS.put("부정", Arrays.asList("나쁘", "불만", "실망"));
}
```

```java
private static final Map<String, List<String>> S_KEYWORDS = Map.of(
        "긍정", List.of("좋아요", "만족"),
        "중립", List.of("괜찮", "보통"),
        "부정", List.of("불만", "최악")
);
```

문제점:

- 분석과 필터링이 서로 다른 키워드 출처를 사용할 수 있음
- 키워드 변경 시 여러 파일을 수정해야 함
- `중립`처럼 우선순위가 중요한 규칙이 여러 곳에 흩어짐

### After

`SentimentKeywordFileDb`가 CSV에서 감정 키워드를 읽고, `TextAnalyzer`와 `Filters`가 같은 판정 로직을 사용한다.

```java
@Service
public class SentimentKeywordFileDb {
    private static final String DEFAULT_FILE_PATH = "data/sentiment_keywords.csv";
    private static final List<String> SENTIMENT_PRIORITY = List.of(
            Constants.SENTIMENT_NEUTRAL,
            Constants.SENTIMENT_NEGATIVE,
            Constants.SENTIMENT_POSITIVE
    );

    public String detectSentiment(String text) {
        String normalizedText = text.toLowerCase();
        Map<String, List<String>> keywords = loadKeywords();

        for (String sentiment : SENTIMENT_PRIORITY) {
            if (containsAnyKeyword(normalizedText, keywords.getOrDefault(sentiment, Collections.emptyList()))) {
                return sentiment;
            }
        }

        return Constants.SENTIMENT_NEUTRAL;
    }
}
```

```java
private String detectSentiment(String normalizedFeedbackText) {
    return sentimentKeywordFileDb.detectSentiment(normalizedFeedbackText);
}
```

```java
private String getSentiment(String text) {
    return sentimentKeywordFileDb.detectSentiment(text);
}
```

CSV 포맷은 다음과 같다.

```csv
sentiment,keyword
긍정,좋아요
중립,보통
부정,불만
```

개선 효과:

- 감정 분석과 감정 필터링의 규칙 출처 통합
- 키워드 변경 시 CSV만 수정하면 됨
- `중립 -> 부정 -> 긍정` 우선순위를 한 곳에서 관리

## 8. Trend 시각화 추가

### Before

기존 화면은 현재 입력 또는 업로드된 피드백의 감정/키워드 분포만 보여주었다. 날짜별 감정 변화 추이를 표시하는 구조는 없었다.

```html
<div class="section" th:if="${sentimentResults} or ${keywordResults}">
    <h3>분석 결과</h3>
    <!-- 감정 분포와 키워드 분포만 표시 -->
</div>
```

### After

`FeedbackTrendFileDb`가 `test_feedback_trend.csv`를 읽고, Controller 공통 모델 속성으로 `trendPoints`를 전달한다.

```java
public List<FeedbackTrendFileDb.TrendPoint> getTrendPoints() {
    return feedbackTrendFileDb.getTrendPoints();
}
```

```java
private void addCommonModelAttributes(Model model) {
    model.addAttribute("categories", uiComponents.getCategories());
    model.addAttribute("logLevel", logger.getLogLevel());
    model.addAttribute("trendPoints", feedbackService.getTrendPoints());
}
```

화면에는 날짜별 누적 막대가 표시된다.

```html
<div class="section" th:if="${trendPoints}">
    <h3>피드백 Trend</h3>
    <p>test_feedback_trend.csv 기준 감정 변화</p>
    <div class="trend-row" th:each="point : ${trendPoints}">
        <strong th:text="${point.date}"></strong>
        <div class="trend-bar">
            <div class="trend-segment-positive"
                 th:style="'width:' + ${point.total == 0 ? 0 : point.positive * 100 / point.total} + '%'"></div>
            <div class="trend-segment-neutral"
                 th:style="'width:' + ${point.total == 0 ? 0 : point.neutral * 100 / point.total} + '%'"></div>
            <div class="trend-segment-negative"
                 th:style="'width:' + ${point.total == 0 ? 0 : point.negative * 100 / point.total} + '%'"></div>
        </div>
        <span th:text="${point.total} + '건'"></span>
    </div>
</div>
```

개선 효과:

- 날짜별 긍정/중립/부정 변화 추이를 화면에서 확인 가능
- Trend 데이터 로딩 책임이 별도 File DB 클래스로 분리됨
- 테스트에서 CSV 로딩과 `TrendPoint` 값을 직접 검증 가능

## 9. 현재 구조 요약

리팩토링 후 주요 책임은 다음과 같이 정리되었다.

- `FeedbackController`: HTTP endpoint, 요청 파라미터 수신, View Model 구성
- `FeedbackService`: 피드백 입력/업로드/분석/필터링 흐름 조율
- `TextAnalyzer`: 감정 카운트와 카테고리 키워드 카운트 계산
- `Filters`: 감정/카테고리 조건 기반 피드백 필터링
- `SentimentKeywordFileDb`: 감정 키워드 CSV 로딩과 감정 판정
- `FeedbackTrendFileDb`: Trend CSV 로딩과 화면 표시용 데이터 제공
- `DemoApplicationTests`: 주요 분석/필터/File DB 동작 회귀 테스트

## 10. 검증 결과

기존 보고서 기준으로 각 단계에서 `mvn test`가 성공했다.

- 테스트 구조 개선 후: 9개 테스트 통과
- 버그 수정 및 UI 개선 후: 12개 테스트 통과
- 네이밍/긴 함수/SRP 리팩토링 후: 12개 테스트 통과
- File DB 및 Trend 기능 추가 후: 14개 테스트 통과

현재 문서는 코드 변경 없이 산출물 문서만 추가했으므로 별도 테스트 실행은 수행하지 않았다.

## 11. 남은 개선 과제

이번 리팩토링 이후에도 다음 과제는 남아 있다.

1. `FeedbackService`를 입력/조회/분석/필터링 단위의 더 작은 서비스로 분리
2. static mutable state를 사용하는 `Session` 구조를 Spring `HttpSession` 또는 저장소 기반 구조로 교체
3. `TextAnalyzer`의 static 분석 결과 상태 제거 또는 별도 상태 저장 컴포넌트로 이동
4. `Constants.CATEGORY_KEYWORDS`의 `Map<String, Map<String, Object>>` 구조를 명시적 타입으로 교체
5. 카테고리 키워드도 File DB 또는 설정 파일 기반으로 이동
6. CSV 업로드와 Trend CSV 값에 대한 빈 행, 누락 컬럼, 숫자 파싱 오류 검증 강화
7. Controller MVC 테스트로 `/analyze`, `/upload`, `/filter`, `/log-level` 흐름 검증

## 12. 결론

이번 리팩토링은 기능을 크게 바꾸기보다 기존 피드백 분석기의 구조를 읽기 쉽고 테스트하기 쉬운 방향으로 정리한 작업이다.

가장 큰 변화는 Controller 중심 구조에서 Service 중심 구조로 책임을 이동한 점과, 감정 키워드 규칙을 코드 내부 중복 정의에서 CSV 기반 File DB로 통합한 점이다. 그 결과 분석/필터링 규칙의 일관성이 높아졌고, 핵심 로직은 단위 테스트로 검증 가능한 형태에 가까워졌다.

