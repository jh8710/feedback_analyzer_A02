# Feedback Analyzer Design

## 1. 프로젝트 개요

Feedback Analyzer는 자연어 기반 고객 피드백 데이터를 수집, 분류, 필터링, 시각화하는 Spring Boot + Thymeleaf 기반 웹 애플리케이션이다.

사용자는 웹 화면에서 피드백 텍스트를 직접 입력하거나 CSV 파일을 업로드할 수 있으며, 시스템은 키워드 기반으로 감정과 카테고리를 분석한다. 분석 결과는 화면에 표시되고, 필터링된 결과는 CSV로 다운로드할 수 있다.

## 2. 주요 기능

- 텍스트 피드백 입력
- CSV 파일 업로드
- 키워드 기반 감정 분석
- 키워드 기반 카테고리 분석
- 감정 및 키워드 기준 필터링
- 분석 결과 화면 표시
- 필터링 결과 CSV 다운로드
- 처리 상태 및 오류 로그 출력

## 3. 실행 및 사용 흐름

### 실행

```bash
mvn spring-boot:run
```

### 기본 사용 흐름

1. 웹 브라우저에서 `http://localhost:8080`에 접속한다.
2. 피드백 텍스트를 입력하거나 CSV 파일을 업로드한다.
3. 입력된 피드백에 대해 감정 분석과 키워드 분석을 수행한다.
4. 감정 또는 키워드 필터를 선택해 결과를 좁힌다.
5. 필요한 경우 필터링 결과를 CSV 파일로 다운로드한다.

### CSV 입력 형식

입력 CSV 파일은 `text` 컬럼을 포함해야 하며, 각 행의 텍스트 값이 피드백 데이터로 사용된다.

## 4. 패키지 및 클래스 구성

기본 패키지는 `com.example.demo`이며, 주요 클래스는 다음과 같다.

| 클래스 | 역할 |
| --- | --- |
| `DemoApplication` | Spring Boot 애플리케이션 시작점 |
| `FeedbackController` | 웹 요청 처리, 분석/업로드/필터/다운로드 흐름 제어 |
| `Feedback` | 피드백 텍스트와 분석 정보를 담는 모델 |
| `TextAnalyzer` | 감정 분석 및 카테고리 키워드 분석 |
| `Filters` | 감정과 카테고리 기준으로 피드백 필터링 |
| `Constants` | 감정 및 카테고리 분석에 사용하는 키워드 상수 |
| `Session` | 현재 피드백 목록과 내부 상태를 정적 필드로 관리 |
| `FileHandler` | 분석 결과 저장용 서비스 |
| `UIComponents` | 화면에 제공할 카테고리 목록 구성 |
| `Logger` | 콘솔 기반 로그 출력 유틸리티 |

## 5. 요청 처리 구조

### 메인 화면

`GET /` 요청은 `FeedbackController.index()`에서 처리한다. 세션 상태를 초기화하고 현재 피드백 목록과 카테고리 목록을 모델에 담아 `index` 뷰를 반환한다.

### 텍스트 분석

`POST /analyze` 요청은 `FeedbackController.analyze()`에서 처리한다. 입력 텍스트가 비어 있지 않으면 `Feedback` 객체로 추가하고, `TextAnalyzer.sent()`와 `TextAnalyzer.kw()`를 호출해 감정 및 카테고리 분석 결과를 계산한다.

### CSV 업로드

`POST /upload` 요청은 `FeedbackController.uploadFile()`에서 처리한다. 업로드된 CSV 파일을 임시 파일로 저장한 뒤 OpenCSV의 `CSVReader`로 읽어 각 행의 첫 번째 컬럼을 피드백으로 추가한다.

### 필터링

`POST /filter` 요청은 `FeedbackController.filter()`에서 처리한다. 현재 피드백 목록을 `Filters.fil()`에 전달해 감정 필터와 키워드 필터를 순서대로 적용한다.

### 다운로드

`GET /download` 요청은 `FeedbackController.downloadFile()`에서 처리한다. 마지막 필터링 결과를 UTF-8 BOM이 포함된 CSV 응답으로 내려준다.

## 6. 분석 로직

### 감정 분석

`TextAnalyzer.sent()`는 각 피드백 텍스트에 대해 `Constants.SENTIMENT_KEYWORDS`를 검사한다.

- 긍정 키워드가 포함되면 `긍정`
- 부정 키워드가 포함되면 `부정`
- 어느 키워드에도 해당하지 않으면 `중립`

분석 결과는 감정별 건수 맵으로 반환된다.

### 카테고리 분석

`TextAnalyzer.kw()`는 `Constants.CATEGORY_KEYWORDS`의 `main` 키워드 목록을 기준으로 피드백 텍스트를 검사한다. 텍스트에 특정 카테고리 키워드가 포함되면 해당 카테고리 건수를 증가시킨다.

현재 카테고리는 다음과 같다.

- 배송
- 품질
- 가격
- 서비스
- 사용성

### 필터링

`Filters.fil()`은 두 단계로 필터링한다.

1. 감정 필터가 `전체`가 아니면 감정 키워드 기준으로 먼저 필터링한다.
2. 키워드 필터가 `전체`가 아니면 선택한 카테고리의 `sub` 키워드 기준으로 다시 필터링한다.

## 7. 데이터 모델

### `Feedback`

피드백 한 건을 표현하는 모델이다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `text` | `String` | 고객 피드백 본문 |
| `sentiment` | `String` | 감정 분석 결과 |
| `category` | `String` | 카테고리 분석 결과 |

현재 구현에서는 `getText()`만 외부에서 사용되며, `sentiment`와 `category`는 생성자에서 받을 수 있지만 별도 getter/setter는 제공되지 않는다.

## 8. 상태 관리

`Session` 클래스는 다음 정적 필드로 애플리케이션 상태를 관리한다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `currentFeedbacks` | `List<Feedback>` | 현재 입력 또는 업로드된 피드백 목록 |
| `internalData` | `Map<String, Object>` | 내부 상태 저장용 맵 |
| `filterOptions` | `Map<String, Object>` | 필터 옵션 저장용 맵 |

현재 구현은 HTTP 세션 객체를 직접 사용하는 방식이 아니라, 애플리케이션 전역 정적 필드로 상태를 유지한다.

## 9. 메서드 목록

### `DemoApplication`

| 메서드 | 설명 |
| --- | --- |
| `main(String[] args)` | Spring Boot 애플리케이션을 시작한다. |

### `FeedbackController`

| 메서드 | HTTP 매핑 | 설명 |
| --- | --- | --- |
| `index(Model model)` | `GET /` | 메인 화면을 표시하고 현재 피드백 및 카테고리 목록을 모델에 추가한다. |
| `analyze(String text, Model model)` | `POST /analyze` | 텍스트 입력을 피드백 목록에 추가하고 감정/키워드 분석 결과를 계산한다. |
| `uploadFile(MultipartFile file, Model model)` | `POST /upload` | 업로드된 CSV 파일을 읽어 피드백 목록에 추가한다. |
| `filter(String sentiment, String keyword, Model model)` | `POST /filter` | 감정 및 키워드 조건에 따라 피드백 목록을 필터링한다. |
| `downloadFile(HttpServletResponse res)` | `GET /download` | 필터링된 피드백 목록을 CSV 파일로 다운로드한다. |

### `Feedback`

| 메서드 | 설명 |
| --- | --- |
| `Feedback()` | 기본 생성자이다. |
| `Feedback(String text)` | 피드백 텍스트를 받는 생성자이다. |
| `Feedback(String text, String sentiment, String category)` | 피드백 텍스트, 감정, 카테고리를 받는 생성자이다. |
| `getText()` | 피드백 텍스트를 반환한다. |

### `TextAnalyzer`

| 메서드 | 설명 |
| --- | --- |
| `sent(List<Feedback> feedbacks)` | 피드백 목록을 감정 키워드로 분석하고 감정별 건수를 반환한다. |
| `kw(List<Feedback> feedbacks)` | 피드백 목록을 카테고리 키워드로 분석하고 카테고리별 건수를 반환한다. |

### `Filters`

| 메서드 | 설명 |
| --- | --- |
| `fil(List<Feedback> dataList, String sFilter, String kFilter)` | 감정 필터와 키워드 필터를 적용한 피드백 목록을 반환한다. |

### `Constants`

| 구성 요소 | 설명 |
| --- | --- |
| `SENTIMENT_KEYWORDS` | 긍정/부정 감정 판별에 사용하는 키워드 맵이다. |
| `CATEGORY_KEYWORDS` | 배송/품질/가격/서비스/사용성 카테고리 판별에 사용하는 키워드 맵이다. |

### `Session`

| 메서드 | 설명 |
| --- | --- |
| `initSessionStateUgly()` | 정적 상태 필드가 `null`이면 기본 컬렉션으로 초기화한다. |
| `getOldDataFromSession(String key)` | 지정 키가 `current_feedbacks`이면 현재 피드백 목록을 반환한다. |
| `updateInternalData(String key, Object value)` | 내부 데이터 맵을 갱신하고, 키가 `current_feedbacks`이면 현재 피드백 목록도 갱신한다. |
| `getCurrentFeedbacks()` | 현재 피드백 목록을 반환한다. |

### `FileHandler`

| 메서드 | 설명 |
| --- | --- |
| `saveResult(List<Feedback> data)` | 피드백 수와 각 피드백 텍스트를 콘솔에 출력한다. |
| `save(List<Feedback> data)` | `saveResult()`를 호출해 결과 저장 처리를 위임한다. |

### `UIComponents`

| 메서드 | 설명 |
| --- | --- |
| `getCategories()` | 화면 필터에 사용할 카테고리 목록을 반환한다. |

### `Logger`

| 메서드 | 설명 |
| --- | --- |
| `logInfo(String message)` | INFO 로그를 출력한다. |
| `logWarning(String message)` | WARNING 로그를 출력한다. |
| `logError(String message)` | ERROR 로그를 출력한다. |
| `logDebug(String message)` | 디버그 모드가 켜져 있으면 DEBUG 로그를 출력한다. |
| `setDebugMode(boolean mode)` | 디버그 모드 사용 여부를 변경한다. |
| `isDebugMode()` | 현재 디버그 모드 상태를 반환한다. |
| `logInfo(String format, Object... args)` | 포맷 문자열 기반 INFO 로그를 출력한다. |
| `logWarning(String format, Object... args)` | 포맷 문자열 기반 WARNING 로그를 출력한다. |
| `logError(String format, Object... args)` | 포맷 문자열 기반 ERROR 로그를 출력한다. |
| `logDebug(String format, Object... args)` | 포맷 문자열 기반 DEBUG 로그를 출력한다. |

## 10. 설계상 참고 사항

- 현재 분석 방식은 머신러닝 모델이 아니라 사전 정의된 키워드 포함 여부를 검사하는 규칙 기반 방식이다.
- 현재 상태 관리는 사용자별 HTTP 세션이 아니라 정적 필드 기반이므로 여러 사용자가 동시에 사용할 경우 데이터가 공유될 수 있다.
- CSV 업로드는 첫 번째 행을 헤더로 보고 건너뛰며, 각 데이터 행의 첫 번째 컬럼을 피드백 텍스트로 사용한다.
- 다운로드 대상은 전체 분석 결과가 아니라 마지막 필터링 결과인 `fil_data`이다.
- `FileHandler`는 서비스로 등록되어 있지만 현재 컨트롤러 흐름에서는 직접 사용되지 않는다.
