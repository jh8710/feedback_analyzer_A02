# 04_REFACTORING-네이밍& 매직넘버개선 보고서

## 1. 작업 개요

이번 세션에서는 `Constants.java`와 `TextAnalyzer.java`를 중심으로 매직넘버와 하드코딩 문자열을 상수로 추출하고, 의미가 모호한 변수명과 메서드명을 도메인 용어로 개선했다.

작업 목표:

- 감성 라벨과 카테고리 라벨 상수화
- 카테고리 키워드 맵의 구조 키 상수화
- 분석 결과 초기 카운트 `0` 상수화
- `TextAnalyzer`의 축약 메서드명과 변수명 개선
- 변경된 공개 메서드명에 맞춘 호출부와 테스트 갱신
- 기존 분석 동작 보존 및 테스트 통과 확인

## 2. 참조 자료

작업에 사용한 주요 파일은 다음과 같다.

- `src/main/java/com/example/demo/Constants.java`
- `src/main/java/com/example/demo/TextAnalyzer.java`
- `src/main/java/com/example/demo/FeedbackController.java`
- `src/test/java/com/example/demo/DemoApplicationTests.java`
- `.cursorrules`

## 3. 기존 상태

기존 `TextAnalyzer.java`는 다음과 같은 코드 스멜을 포함하고 있었다.

- `sent`, `kw`처럼 축약된 공개 메서드명 사용
- `res`, `res2`, `txt`, `cat`, `kws`, `s`처럼 의미 파악이 어려운 변수명 사용
- `"긍정"`, `"중립"`, `"부정"`, `"main"` 같은 도메인 문자열 직접 사용
- 분석 카운트 초기값 `0` 반복 사용
- `globalSent`, `globalKw`처럼 역할이 불명확한 정적 필드명 사용

`Constants.java`에도 카테고리명과 내부 맵 키가 문자열 리터럴로 반복되어 있었다. 이로 인해 오타에 취약하고, 분석 도메인의 핵심 용어가 코드 곳곳에 흩어져 있었다.

## 4. 변경 내용

### 4.1 도메인 문자열 상수화

`Constants.java`에 감성 라벨 상수를 추가했다.

- `SENTIMENT_POSITIVE`
- `SENTIMENT_NEUTRAL`
- `SENTIMENT_NEGATIVE`

카테고리 라벨도 상수로 추출했다.

- `CATEGORY_DELIVERY`
- `CATEGORY_QUALITY`
- `CATEGORY_PRICE`
- `CATEGORY_SERVICE`
- `CATEGORY_USABILITY`

이를 통해 `TextAnalyzer`와 카테고리 키워드 초기화 코드가 동일한 도메인 상수를 참조하도록 정리했다.

### 4.2 카테고리 맵 구조 키 상수화

카테고리 키워드 맵에서 사용하던 `"main"`과 `"sub"`를 다음 상수로 추출했다.

- `CATEGORY_MAIN_KEY`
- `CATEGORY_SUB_KEY`

`TextAnalyzer`는 카테고리 대표 키워드를 조회할 때 `"main"` 문자열 대신 `Constants.CATEGORY_MAIN_KEY`를 사용하도록 변경했다.

### 4.3 분석 초기 카운트 상수화

감성 분석과 카테고리 분석에서 반복되던 초기값 `0`을 `INITIAL_ANALYSIS_COUNT`로 추출했다.

변경 전에는 각 분석 결과 맵에 직접 `0`을 넣었지만, 변경 후에는 초기 카운트의 의미가 코드에 드러나도록 했다.

### 4.4 `TextAnalyzer` 네이밍 개선

공개 메서드명을 분석 도메인에 맞게 변경했다.

- `sent` -> `analyzeSentiments`
- `kw` -> `analyzeCategoryKeywords`

내부 변수명도 의미 중심으로 변경했다.

- `res` -> `sentimentCounts`
- `res2` -> `categoryCounts`
- `f` -> `feedback`
- `txt` -> `normalizedFeedbackText`
- `s` -> `detectedSentiment`
- `entry` -> `categoryEntry`
- `cat` -> `categoryName`
- `kws` -> `categoryMainKeywords`
- `globalSent` -> `latestSentimentCounts`
- `globalKw` -> `latestCategoryCounts`

이 변경은 분석 로직의 동작을 바꾸지 않고, 코드에서 어떤 값이 어떤 도메인 개념을 의미하는지 더 명확히 드러내는 데 목적이 있다.

### 4.5 호출부 및 테스트 갱신

`TextAnalyzer`의 공개 메서드명이 변경되었기 때문에 다음 파일의 호출부를 함께 갱신했다.

- `FeedbackController.java`
- `DemoApplicationTests.java`

컨트롤러는 기존과 동일하게 분석 결과를 모델에 담지만, 호출 메서드명이 `analyzeSentiments`, `analyzeCategoryKeywords`로 바뀌었다.

테스트도 변경된 메서드명을 사용하도록 수정하여 기존 분석 결과 검증을 유지했다.

## 5. 검증 결과

수정 후 `ReadLints` 기준 편집 파일에 linter 오류가 없음을 확인했다.

실행한 테스트:

```bash
mvn test
```

결과:

- Tests run: 12
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

테스트 실행 과정에서 `target` 하위 빌드 산출물이 변경되었으나, 소스 변경 범위와 무관한 생성물이므로 되돌렸다.

## 6. 최종 변경 파일

최종적으로 소스 변경이 남은 파일은 다음과 같다.

- `src/main/java/com/example/demo/Constants.java`
- `src/main/java/com/example/demo/TextAnalyzer.java`
- `src/main/java/com/example/demo/FeedbackController.java`
- `src/test/java/com/example/demo/DemoApplicationTests.java`

## 7. 결론

이번 리팩터링은 기능 변경 없이 분석 도메인의 핵심 용어를 코드에 명확히 반영하는 작업이었다.

매직넘버와 하드코딩 문자열을 상수화하여 오타와 중복 위험을 줄였고, `TextAnalyzer`의 축약명을 도메인 중심 이름으로 바꿔 감성 분석과 카테고리 분석 흐름을 더 쉽게 읽을 수 있도록 개선했다.
