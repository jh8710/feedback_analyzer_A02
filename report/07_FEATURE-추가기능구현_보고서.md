# 07_FEATURE-추가기능구현 보고서

## 1. 작업 개요

이번 세션에서는 피드백 분석기에 다음 추가 기능을 구현했다.

작업 목표:

- `test_feedback_trend.csv`를 기반으로 피드백 감정 Trend 시각화 추가
- 감정 분석 및 감정 필터 키워드를 File DB로 관리하도록 변경
- 기존 분석/필터링 흐름과 화면 동작 유지
- CSV 기반 데이터 로딩 로직에 대한 테스트 추가
- 전체 테스트 실행으로 회귀 여부 확인

## 2. 참조 자료

구현과 검증에 사용한 주요 파일은 다음과 같다.

- `src/main/java/com/example/demo/FeedbackController.java`
- `src/main/java/com/example/demo/FeedbackService.java`
- `src/main/java/com/example/demo/TextAnalyzer.java`
- `src/main/java/com/example/demo/Filters.java`
- `src/main/java/com/example/demo/SentimentKeywordFileDb.java`
- `src/main/java/com/example/demo/FeedbackTrendFileDb.java`
- `src/main/resources/templates/index.html`
- `src/main/resources/application.properties`
- `data/sentiment_keywords.csv`
- `data/test_feedback_trend.csv`
- `src/main/resources/data/sentiment_keywords.csv`
- `src/main/resources/data/test_feedback_trend.csv`
- `src/test/java/com/example/demo/DemoApplicationTests.java`

## 3. 기존 상태

기존 시스템은 감정 분석과 필터링을 수행할 수 있었지만, 감정 키워드 관리와 Trend 표시 측면에서 다음 한계가 있었다.

### 3.1 Trend 시각화 부재

화면에는 현재 입력 또는 업로드된 피드백의 감정 분포와 키워드 분포만 표시되었다.

한계:

- 날짜별 감정 변화 추이를 볼 수 없음
- `project_purpose.md`에 언급된 `test_feedback_trend.csv` 기반 Trend 기능이 구현되지 않음
- 별도 CSV 데이터를 화면에 표시하는 데이터 로딩 경로가 없음

### 3.2 감정 키워드 하드코딩

감정 분석과 감정 필터링 키워드는 코드 내부에 분산되어 있었다.

주요 문제:

- `Constants.SENTIMENT_KEYWORDS`는 감정 분석에 사용됨
- `Filters` 내부 `S_KEYWORDS`는 감정 필터링에 별도 정의되어 있었음
- 같은 도메인 규칙이 여러 곳에 중복되어 키워드 변경 시 수정 범위가 커짐
- 분석 결과와 필터 결과가 서로 달라질 위험이 있음

## 4. 변경 내용

### 4.1 `SentimentKeywordFileDb` 추가

감정 키워드를 CSV 파일에서 읽는 File DB 클래스를 추가했다.

역할:

- `data/sentiment_keywords.csv`에서 감정별 키워드 로딩
- 외부 파일이 없을 경우 classpath 리소스 `src/main/resources/data/sentiment_keywords.csv` 로딩
- 파일과 리소스가 모두 없을 경우 기존 상수 기반 기본값 사용
- 감정 라벨 목록 제공
- 텍스트의 감정 판정 제공

CSV 포맷:

```csv
sentiment,keyword
긍정,좋아요
중립,보통
부정,불만
```

감정 판정 우선순위는 기존 필터 버그 수정 결과를 보존하기 위해 다음 순서로 유지했다.

1. 중립
2. 부정
3. 긍정

어떤 키워드도 매칭되지 않으면 기존 동작과 동일하게 `중립`으로 처리한다.

### 4.2 `TextAnalyzer`와 `Filters`의 감정 규칙 통합

`TextAnalyzer`와 `Filters`가 모두 `SentimentKeywordFileDb`를 사용하도록 변경했다.

변경 전:

- `TextAnalyzer`는 `Constants.SENTIMENT_KEYWORDS`를 직접 사용
- `Filters`는 내부 static Map인 `S_KEYWORDS`를 별도로 사용

변경 후:

- `TextAnalyzer.analyzeSentiments()`는 File DB에서 감정 라벨을 읽어 카운트를 초기화
- `TextAnalyzer`의 감정 판정은 `SentimentKeywordFileDb.detectSentiment()`에 위임
- `Filters`의 감정 필터 판정도 같은 `detectSentiment()`에 위임

변경 효과:

- 감정 분석과 필터링의 키워드 출처를 통합
- 키워드 변경 시 CSV 파일만 수정하면 됨
- 코드 내부 중복 키워드 정의 제거

### 4.3 감정 키워드 File DB 파일 추가

다음 위치에 감정 키워드 CSV를 추가했다.

- `data/sentiment_keywords.csv`
- `src/main/resources/data/sentiment_keywords.csv`

두 위치를 둔 이유:

- `data/` 파일은 애플리케이션 실행 디렉터리 기준 외부 File DB로 우선 사용
- `src/main/resources/data/` 파일은 패키징 및 테스트 환경에서 fallback 리소스로 사용

설정값:

```properties
feedback.sentiment-keywords.path=data/sentiment_keywords.csv
```

### 4.4 `FeedbackTrendFileDb` 추가

Trend CSV를 읽는 File DB 클래스를 추가했다.

역할:

- `data/test_feedback_trend.csv`에서 날짜별 감정 카운트 로딩
- 외부 파일이 없을 경우 classpath 리소스 `src/main/resources/data/test_feedback_trend.csv` 로딩
- 화면 표시용 `TrendPoint` 객체 제공

CSV 포맷:

```csv
date,positive,neutral,negative
2026-05-16,12,5,3
2026-05-17,14,4,4
```

`TrendPoint` 제공 값:

- `date`
- `positive`
- `neutral`
- `negative`
- `total`

### 4.5 Trend 데이터 Service 연결

`FeedbackService`에 `FeedbackTrendFileDb`를 주입하고, Controller에서 사용할 수 있도록 `getTrendPoints()` 메서드를 추가했다.

`FeedbackController.addCommonModelAttributes()`는 모든 화면 렌더링에서 다음 공통 모델 속성을 추가한다.

- `categories`
- `logLevel`
- `trendPoints`

이로써 메인 진입, 분석, 업로드, 필터링 이후에도 Trend 영역이 동일하게 표시된다.

### 4.6 화면 Trend 시각화 추가

`index.html`에 Trend 섹션을 추가했다.

표시 방식:

- 날짜별 행 표시
- 긍정, 중립, 부정 카운트를 누적 막대 형태로 시각화
- 각 날짜의 총 피드백 수 표시
- 색상 범례 제공

색상:

- 긍정: 초록
- 중립: 노랑
- 부정: 빨강

## 5. 최종 구조

추가 기능 구현 후 주요 책임은 다음과 같다.

### 5.1 `SentimentKeywordFileDb`

감정 키워드 File DB 담당.

- CSV 파일 로딩
- 감정 라벨 조회
- 감정 키워드 조회
- 감정 판정

### 5.2 `FeedbackTrendFileDb`

Trend File DB 담당.

- 날짜별 감정 카운트 CSV 로딩
- 화면 시각화용 Trend 데이터 제공

### 5.3 `TextAnalyzer`

분석 흐름 담당.

- 감정 카운트 계산
- 카테고리 키워드 카운트 계산
- 감정 판정 세부 규칙은 `SentimentKeywordFileDb`에 위임

### 5.4 `Filters`

피드백 필터링 담당.

- 감정 조건 필터링
- 카테고리 조건 필터링
- 감정 판정 세부 규칙은 `SentimentKeywordFileDb`에 위임

### 5.5 `FeedbackService`

화면에 필요한 유스케이스 조합 담당.

- 피드백 입력/업로드/필터링
- 분석 결과 생성
- Trend 데이터 조회

## 6. 테스트 추가

`DemoApplicationTests.java`에 다음 테스트를 추가했다.

### 6.1 `sentimentKeywordFileDbLoadsKeywordsFromCsvFile`

검증 내용:

- 임시 감정 키워드 CSV 파일 생성
- `SentimentKeywordFileDb`가 파일에서 키워드를 읽는지 확인
- 긍정, 중립, 부정 판정이 CSV 내용 기준으로 동작하는지 확인

### 6.2 `feedbackTrendFileDbLoadsTrendPointsFromCsvFile`

검증 내용:

- 임시 Trend CSV 파일 생성
- `FeedbackTrendFileDb`가 날짜별 감정 카운트를 읽는지 확인
- `date`, `positive`, `neutral`, `negative`, `total` 값 검증

## 7. 테스트 실행 결과

다음 명령으로 전체 테스트를 실행했다.

```bash
mvn test
```

결과:

- Tests run: 14
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

참고:

- Maven 컴파일 출력에 `Session.java uses unchecked or unsafe operations` 경고가 표시되었으나, 이번 기능 구현 범위 밖의 기존 경고로 판단했다.
- 테스트 실행으로 `target/` 빌드 산출물이 갱신되었다.

## 8. Linter 확인

수정 및 추가된 주요 소스 파일을 대상으로 IDE 진단을 확인했다.

대상:

- `src/main/java/com/example/demo/SentimentKeywordFileDb.java`
- `src/main/java/com/example/demo/FeedbackTrendFileDb.java`
- `src/main/java/com/example/demo/TextAnalyzer.java`
- `src/main/java/com/example/demo/Filters.java`
- `src/test/java/com/example/demo/DemoApplicationTests.java`

결과:

- linter 오류 없음

## 9. 요구사항 충족 여부

### 9.1 `test_feedback_trend.csv`로 Trend 시각화 추가

충족했다.

- `test_feedback_trend.csv` 파일 추가
- CSV 로딩용 `FeedbackTrendFileDb` 추가
- `FeedbackService`와 `FeedbackController`를 통해 화면 모델에 연결
- `index.html`에 Trend 누적 막대 시각화 추가

### 9.2 감정 분석 필터를 File DB로 관리하도록 변경

충족했다.

- `sentiment_keywords.csv` 파일 추가
- `SentimentKeywordFileDb` 추가
- `TextAnalyzer`와 `Filters`가 동일 File DB를 사용하도록 변경
- 감정 분석과 감정 필터링 키워드 출처 통합

## 10. 작업 중 발견한 사항

감정 키워드와 카테고리 키워드는 아직 완전히 같은 방식으로 관리되지 않는다.

- 감정 키워드: File DB로 이동
- 카테고리 키워드: `Constants.CATEGORY_KEYWORDS`에 유지

이번 요청은 감정 분석 필터의 File DB 관리가 목적이므로 카테고리 키워드는 변경하지 않았다.

또한 CSV File DB는 요청마다 파일을 다시 읽는 단순 구조다. 현재 규모에서는 이해하기 쉽고 테스트하기 쉬운 방식이지만, 데이터가 커지거나 요청 수가 많아지면 캐싱 또는 변경 감지 전략을 추가할 수 있다.

## 11. 최종 산출물

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

## 12. 권장 후속 작업

1. 카테고리 키워드도 File DB 또는 설정 파일 기반으로 통합
2. File DB 로딩 결과 캐싱 및 파일 변경 감지 추가
3. Trend CSV 값 검증 강화
4. Trend 시각화를 Chart.js 같은 차트 라이브러리로 개선
5. Controller MVC 테스트로 Trend 모델 속성 검증
6. `target/` 빌드 산출물은 Git 관리 대상에서 제외하도록 정리
