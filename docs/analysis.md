# Feedback Analyzer 프로젝트 구조 및 문제점 분석

## 1. 프로젝트 개요

이 프로젝트는 고객 피드백을 직접 입력하거나 CSV로 업로드한 뒤, 키워드 기반으로 감정과 카테고리를 집계하고 필터링 결과를 CSV로 다운로드하는 Spring Boot + Thymeleaf 웹 애플리케이션입니다.

`project_purpose.md`에 따르면 이 코드는 리팩토링 실습을 위해 의도적으로 코드 스멜과 안티패턴을 포함한 초기 버전입니다. 실제 코드도 컨트롤러 중심 구조, 전역 상태, 하드코딩된 분석 규칙, 테스트 부족 등 리팩토링 학습에 적합한 문제를 다수 포함하고 있습니다.

## 2. 기술 스택 및 빌드 구성

- Java 17
- Spring Boot 3.5.3
- Spring Web
- Thymeleaf
- Spring Boot Test
- OpenCSV 5.11.2
- Maven

주요 설정은 `pom.xml`과 `src/main/resources/application.properties`에 있습니다. 서버 포트는 `8080`, 애플리케이션 이름은 `demo`로 설정되어 있습니다.

## 3. 전체 디렉터리 구조

```text
feedback_analyzer_A02/
├── pom.xml
├── README.md
├── project_purpose.md
├── feedback_analyzer.png
├── docs/
│   └── analysis.md
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── DemoApplication.java
│   │   │   ├── FeedbackController.java
│   │   │   ├── TextAnalyzer.java
│   │   │   ├── Filters.java
│   │   │   ├── Constants.java
│   │   │   ├── Feedback.java
│   │   │   ├── FileHandler.java
│   │   │   ├── Logger.java
│   │   │   ├── Session.java
│   │   │   └── UIComponents.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/index.html
│   └── test/java/com/example/demo/
│       └── DemoApplicationTests.java
└── target/
    └── 빌드 산출물
```

현재 Git 상태에는 `target/classes`, `target/test-classes`, `target/maven-status` 같은 빌드 산출물이 변경/추가 파일로 잡혀 있습니다. 일반적으로 `target/`은 Git 추적 대상에서 제외해야 합니다.

## 4. 주요 클래스별 역할

### `DemoApplication`

Spring Boot 애플리케이션 시작점입니다. `SpringApplication.run()`만 수행합니다.

### `FeedbackController`

웹 요청 대부분을 처리하는 핵심 컨트롤러입니다.

- `GET /`: 초기 화면 렌더링
- `POST /analyze`: 수동 입력 피드백 저장 및 분석
- `POST /upload`: CSV 파일 업로드 및 피드백 저장
- `POST /filter`: 감정/카테고리 필터링
- `GET /download`: 필터링 결과 CSV 다운로드

현재 요청 처리, 세션 상태 접근, CSV 파일 처리, 분석 호출, 필터링 결과 보관, 다운로드 응답 생성이 모두 이 클래스에 집중되어 있습니다.

### `TextAnalyzer`

`Feedback` 목록을 입력받아 키워드 포함 여부로 감정과 카테고리 통계를 계산합니다.

- `sent()`: 긍정/중립/부정 개수 집계
- `kw()`: 배송/품질/가격/서비스/사용성 카테고리 개수 집계

분석 규칙은 `Constants.SENTIMENT_KEYWORDS`, `Constants.CATEGORY_KEYWORDS`에 의존합니다.

### `Filters`

사용자가 선택한 감정과 카테고리 조건으로 피드백 목록을 필터링합니다. 감정 판별용 키워드를 내부 static map으로 별도 보유하고 있어 `TextAnalyzer`와 기준이 다릅니다.

### `Constants`

감정 키워드와 카테고리 키워드를 코드에 직접 정의합니다. 중복 키워드가 있고, `Map<String, Object>` 형태라 타입 안정성이 낮습니다.

### `Session`

실제 HTTP 세션이 아니라 static 필드 기반 전역 상태 저장소입니다.

- `currentFeedbacks`: 현재 피드백 목록
- `internalData`: 임의 데이터 저장용 map
- `filterOptions`: 선언만 되고 실제 활용은 거의 없음

### `Feedback`

피드백 데이터를 표현하는 모델입니다. `text`, `sentiment`, `category` 필드가 있지만 `getText()` 외 getter/setter가 없고, 실제 분석 결과 저장에도 거의 쓰이지 않습니다.

### `FileHandler`

`save()`와 `saveResult()`가 있지만 현재 실질적인 파일 저장 기능은 없고 콘솔 출력만 수행합니다. `FeedbackController`에 주입되어 있으나 사용되지 않습니다.

### `Logger`

Spring Boot의 기본 로깅 체계 대신 `System.out`과 `System.err`로 로그를 출력하는 자체 로거입니다. static 메서드와 Spring Bean 사용이 혼재되어 있습니다.

### `UIComponents`

화면에서 사용할 카테고리 목록을 제공합니다. 카테고리 목록이 `Constants.CATEGORY_KEYWORDS`와 별도로 관리되어 변경 시 불일치 가능성이 있습니다.

### `index.html`

Thymeleaf 기반 단일 화면입니다. 피드백 입력, CSV 업로드, 필터 선택, 분석 결과 표시, 다운로드 버튼을 모두 포함합니다.

### `DemoApplicationTests`

Spring Context 로드 여부만 확인합니다. 실제 도메인 로직, 컨트롤러, CSV 처리, 예외 케이스 검증은 없습니다.

## 5. 주요 실행 흐름

### 수동 입력 분석

1. 사용자가 `/`에서 피드백 텍스트를 입력합니다.
2. `POST /analyze`가 호출됩니다.
3. `FeedbackController`가 `Session.getCurrentFeedbacks()`로 전역 피드백 목록을 가져옵니다.
4. 입력값이 비어 있지 않으면 `new Feedback(text.trim())`를 목록에 추가합니다.
5. `TextAnalyzer.sent()`와 `TextAnalyzer.kw()`로 통계를 계산합니다.
6. 계산 결과와 피드백 목록을 모델에 담아 `index.html`을 다시 렌더링합니다.

### CSV 업로드

1. 사용자가 CSV 파일을 업로드합니다.
2. `POST /upload`가 호출됩니다.
3. 업로드 파일을 `C:\tmp\{originalFilename}` 경로에 임시 저장합니다.
4. OpenCSV `CSVReader`로 첫 줄을 건너뛰고 각 행의 첫 번째 컬럼을 `Feedback`으로 추가합니다.
5. 전역 피드백 목록을 갱신하고 화면에 결과를 표시합니다.

### 필터링 및 다운로드

1. 사용자가 감정과 키워드 필터를 선택합니다.
2. `POST /filter`가 호출됩니다.
3. `Filters.fil()`이 조건에 맞는 피드백 목록을 반환합니다.
4. 컨트롤러 필드 `fil_data`에 필터 결과를 저장합니다.
5. 사용자가 `/download`를 호출하면 `fil_data`를 CSV로 출력합니다.

## 6. 주요 문제점

### 6.1. 아키텍처와 책임 분리 부족

`FeedbackController`가 너무 많은 책임을 가지고 있습니다. 컨트롤러는 요청/응답 조립에 집중해야 하지만 현재는 입력 검증, 상태 저장, CSV 파싱, 분석 실행, 필터링 결과 보관, CSV 다운로드까지 모두 담당합니다.

이 구조에서는 기능 추가나 테스트가 어려워지고, 파일 처리나 분석 정책 변경이 웹 계층 코드 변경으로 이어집니다.

### 6.2. static 전역 상태 사용

`Session.currentFeedbacks`는 모든 사용자와 모든 요청이 공유하는 전역 리스트입니다. `FeedbackController.fil_data`도 컨트롤러 Bean의 인스턴스 필드이므로 애플리케이션 전체에서 공유됩니다.

이 때문에 다음 문제가 발생할 수 있습니다.

- 사용자 A가 입력한 피드백이 사용자 B에게 보일 수 있음
- 동시에 여러 요청이 들어오면 데이터가 섞이거나 손실될 수 있음
- 필터링 결과 다운로드가 다른 사용자의 결과로 바뀔 수 있음
- 테스트 간 상태가 격리되지 않음

### 6.3. CSV 업로드 보안 문제

업로드 처리에서 `file.getOriginalFilename()`을 그대로 `C:\tmp\` 경로에 붙여 사용합니다. 원본 파일명은 사용자가 조작할 수 있으므로 신뢰하면 안 됩니다.

주요 위험은 다음과 같습니다.

- 경로 조작 가능성
- 허용되지 않은 확장자 업로드
- 파일 크기 제한 부재
- `C:\tmp` 디렉터리 존재 여부에 대한 처리 부재
- 임시 파일 생성/삭제 정책 미흡
- CSV 컬럼 검증 부재

### 6.4. CSV 파싱과 다운로드 안정성 부족

업로드 CSV는 첫 번째 행을 무조건 헤더로 건너뛰고, 이후 모든 행에서 `line[0]`을 사용합니다. 빈 행, 컬럼 누락, 헤더명 불일치, 인코딩 문제를 처리하지 않습니다.

다운로드 시에는 `wr.println(iter.getText())`로 텍스트를 그대로 출력합니다. 피드백에 쉼표, 줄바꿈, 큰따옴표가 포함되면 CSV 형식이 깨질 수 있습니다.

### 6.5. 분석 기준 불일치

감정 분석은 `TextAnalyzer`가 `Constants.SENTIMENT_KEYWORDS`를 사용하지만, 필터링은 `Filters.S_KEYWORDS`를 사용합니다. 같은 문장이 분석 결과에서는 긍정으로 집계되지만 필터링에서는 다른 감정으로 분류될 수 있습니다.

카테고리 목록도 `Constants.CATEGORY_KEYWORDS`와 `UIComponents.CATS`에 중복 정의되어 있어 변경 시 누락 가능성이 있습니다.

### 6.6. 하드코딩과 타입 안정성 부족

분석 키워드, 카테고리, 하위 카테고리가 모두 Java 코드에 하드코딩되어 있습니다. 또한 `CATEGORY_KEYWORDS`는 `Map<String, Map<String, Object>>` 구조라 사용할 때 형변환이 필요합니다.

이 구조는 다음 문제를 만듭니다.

- 오타나 구조 변경이 런타임 오류로 이어짐
- 카테고리 추가/변경 시 여러 파일 수정 필요
- 설정과 코드가 분리되지 않아 운영 중 정책 변경이 어려움
- 테스트 데이터와 실제 분석 정책 관리가 어려움

### 6.7. 도메인 모델 미완성

`Feedback`에는 `sentiment`, `category` 필드가 있지만 getter/setter가 없고, `TextAnalyzer`도 분석 결과를 객체에 저장하지 않습니다. 현재 `Feedback`은 사실상 텍스트 래퍼에 가깝습니다.

분석 결과를 화면이나 다운로드에서 확장하려면 DTO 또는 도메인 모델을 다시 정리해야 합니다.

### 6.8. 예외 처리와 입력 검증 부족

여러 곳에서 `catch (Exception)`으로 모든 오류를 한 번에 처리합니다. 사용자 입력 오류, 파일 형식 오류, 서버 내부 오류가 구분되지 않습니다.

또한 수동 입력 길이 제한, CSV 행 수 제한, 빈 파일 처리, 필터 파라미터 유효성 검증이 부족합니다.

### 6.9. 로깅 방식 부적절

Spring Boot에서는 일반적으로 SLF4J 기반 로거를 사용하지만, 현재는 자체 `Logger`와 `System.out.println`이 혼재되어 있습니다. 로그 레벨, 포맷, 출력 대상, 운영 환경 설정을 표준 방식으로 제어하기 어렵습니다.

### 6.10. 테스트 부족

현재 테스트는 애플리케이션 컨텍스트 로드만 확인합니다. 핵심 로직에 대한 회귀 방지 장치가 없습니다.

특히 다음 테스트가 필요합니다.

- 감정 분석 단위 테스트
- 카테고리 분석 단위 테스트
- 감정/카테고리 필터링 테스트
- CSV 업로드 성공/실패 테스트
- CSV 다운로드 escaping 테스트
- 다중 사용자 상태 격리 테스트
- 컨트롤러 요청/응답 테스트

### 6.11. Git 관리 문제

현재 작업 트리에 `target/` 하위 빌드 산출물이 변경 파일로 표시됩니다. 빌드 산출물은 재생성 가능한 파일이므로 저장소에 포함하지 않는 것이 일반적입니다.

`.gitignore`에 최소한 다음 항목을 추가하는 것이 좋습니다.

```gitignore
target/
```

이미 추적 중인 산출물이 있다면 Git 인덱스에서 제거하는 별도 정리가 필요합니다.

## 7. 개선 우선순위

### 1순위: 상태 관리와 컨트롤러 책임 분리

`Session`의 static 전역 상태와 `FeedbackController.fil_data`를 먼저 제거해야 합니다. 사용자별 상태가 필요하다면 HTTP session, 서버 저장소, DB, request-scoped model 중 하나를 명확히 선택해야 합니다.

동시에 다음 서비스 계층을 분리하는 것이 좋습니다.

- `FeedbackAnalysisService`: 감정/카테고리 분석
- `FeedbackFilterService`: 필터링
- `FeedbackCsvService`: CSV 업로드/다운로드
- `FeedbackStorage`: 피드백 저장 방식 추상화

### 2순위: CSV 업로드/다운로드 안정화

파일명 신뢰 제거, 확장자/크기 검증, 안전한 임시 파일 생성, CSV 헤더/컬럼 검증이 필요합니다. 다운로드도 OpenCSV 같은 CSV writer를 사용해 escaping을 보장해야 합니다.

### 3순위: 분석 정책 단일화

감정 키워드와 카테고리 키워드를 한 곳에서 관리해야 합니다. `TextAnalyzer`, `Filters`, `UIComponents`가 같은 정책 객체 또는 설정을 바라보게 만들면 분석 결과와 필터 결과의 불일치를 줄일 수 있습니다.

### 4순위: 도메인/DTO 재정의

`Feedback`을 입력 데이터로만 쓸지, 분석 결과까지 담는 모델로 쓸지 결정해야 합니다. 화면 표시와 다운로드를 위해서는 다음과 같은 명시적 결과 DTO가 유용합니다.

- 원문 텍스트
- 감정
- 카테고리 목록
- 매칭된 키워드

### 5순위: 테스트 확충

리팩토링 전 핵심 동작을 보존하기 위해 `TextAnalyzer`와 `Filters` 단위 테스트부터 추가하는 것이 좋습니다. 이후 CSV 처리와 컨트롤러 테스트를 추가하면 구조 변경의 안정성이 높아집니다.

### 6순위: 로깅과 저장소 정리

자체 `Logger`와 `System.out.println`을 SLF4J 로거로 교체하고, `.gitignore`를 추가해 `target/` 산출물을 추적 대상에서 제외해야 합니다.

## 8. 리팩토링 제안 구조

현재 구조를 크게 바꾸지 않으면서도 Spring MVC 프로젝트답게 정리하려면 다음 구조가 적합합니다.

```text
src/main/java/com/example/demo/
├── DemoApplication.java
├── controller/
│   └── FeedbackController.java
├── service/
│   ├── FeedbackAnalysisService.java
│   ├── FeedbackFilterService.java
│   └── FeedbackCsvService.java
├── model/
│   ├── Feedback.java
│   ├── FeedbackAnalysisResult.java
│   └── Sentiment.java
├── config/
│   └── AnalysisPolicy.java
└── storage/
    └── FeedbackStorage.java
```

초기 단계에서는 DB를 도입하기보다 메모리 저장소를 명확히 캡슐화하고, 사용자별 상태 격리가 필요한 시점에 HTTP session 또는 DB로 확장하는 방식이 현실적입니다.

## 9. 결론

현재 프로젝트는 기능은 작지만 리팩토링 포인트가 명확합니다. 가장 큰 문제는 Spring MVC 애플리케이션의 계층 분리가 약하고, 사용자 데이터가 static 전역 상태와 컨트롤러 필드에 저장된다는 점입니다.

따라서 첫 리팩토링은 기능 추가보다 상태 관리, 서비스 분리, 분석 정책 단일화, 테스트 추가에 집중하는 것이 좋습니다. 이 네 가지를 먼저 정리하면 CSV 보안 강화, UI 개선, 분석 정책 확장도 훨씬 안정적으로 진행할 수 있습니다.
