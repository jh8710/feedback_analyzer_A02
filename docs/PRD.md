# Feedback Analyzer PRD

## 1. Product Overview

Feedback Analyzer는 고객 피드백 데이터를 수집, 분류, 시각화하는 Spring Boot + Thymeleaf 기반 웹 애플리케이션이다.

사용자는 웹 대시보드에서 피드백을 직접 입력하거나 CSV 파일로 업로드할 수 있으며, 시스템은 간단한 키워드 기반 규칙으로 감정과 카테고리를 분석한다. 분석 결과는 화면에서 통계 형태로 확인할 수 있고, 필터링된 결과는 CSV로 다운로드할 수 있다.

이 프로젝트는 완성형 서비스라기보다 리팩토링 챌린지 성격이 강하다. 현재 코드는 전역 상태, 중복 로직, 부적절한 책임 분배, 테스트 부족 같은 코드 스멜을 의도적으로 포함하고 있으며, 이를 개선해 테스트 가능하고 유지보수 가능한 구조로 바꾸는 것이 주요 학습 목표다.

## 2. Product Mission

### 2.1 사용자 관점의 미션

- 고객 피드백을 쉽고 빠르게 입력하거나 업로드한다.
- 피드백의 감정 분포를 긍정, 중립, 부정 기준으로 확인한다.
- 피드백의 주요 주제를 배송, 품질, 가격, 서비스, 사용성 기준으로 확인한다.
- 특정 감정이나 카테고리 기준으로 피드백을 필터링한다.
- 필터링된 결과를 CSV 파일로 다운로드한다.

### 2.2 학습 관점의 미션

- 코드 스멜과 안티패턴을 식별한다.
- MVC 또는 레이어드 아키텍처로 책임을 분리한다.
- 비즈니스 로직과 UI 렌더링 책임을 분리한다.
- 중복된 분석/필터링 로직을 통합한다.
- 전역 상태를 제거하고 명시적인 상태 관리 구조를 설계한다.
- 테스트 가능한 구조를 만들고 핵심 로직 테스트를 추가한다.

## 3. Target Users

- 고객 피드백 데이터를 간단히 분류하고 싶은 운영자 또는 관리자
- Java/Spring Boot 기반 리팩토링을 실습하려는 중급 이상 개발자
- Clean Code, TDD, 레이어드 아키텍처, 상태 관리 설계를 학습하려는 개발자

## 4. Core Features

### 4.1 피드백 입력

사용자는 대시보드에서 텍스트 피드백을 직접 입력할 수 있다.

- 입력 방식: 웹 폼 텍스트 입력
- 처리 엔드포인트: `POST /analyze`
- 현재 구현: 입력된 문자열을 `Feedback` 객체로 생성해 전역 세션 상태에 추가

### 4.2 CSV 업로드

사용자는 CSV 파일을 업로드해 여러 피드백을 한 번에 등록할 수 있다.

- 입력 방식: CSV 파일 업로드
- 처리 엔드포인트: `POST /upload`
- 현재 구현: 첫 번째 컬럼을 피드백 텍스트로 간주
- 문서상 필수 컬럼: `text`

### 4.3 감정 분석

시스템은 피드백 텍스트에 포함된 키워드를 기준으로 감정을 분류한다.

- 감정 종류: `긍정`, `중립`, `부정`
- 주요 구현 파일: `src/main/java/com/example/demo/TextAnalyzer.java`
- 키워드 정의 파일: `src/main/java/com/example/demo/Constants.java`

### 4.4 카테고리 분석

시스템은 피드백 텍스트에 포함된 키워드를 기준으로 카테고리별 개수를 집계한다.

- 카테고리 종류: `배송`, `품질`, `가격`, `서비스`, `사용성`
- 주요 구현 파일: `src/main/java/com/example/demo/TextAnalyzer.java`
- 카테고리 키워드 정의: `src/main/java/com/example/demo/Constants.java`

### 4.5 필터링

사용자는 감정과 카테고리를 기준으로 피드백 목록을 필터링할 수 있다.

- 처리 엔드포인트: `POST /filter`
- 주요 구현 파일: `src/main/java/com/example/demo/Filters.java`
- 필터 조건: 감정, 키워드 카테고리

### 4.6 결과 시각화

분석 결과는 Thymeleaf 템플릿에서 통계 카드 형태로 표시된다.

- 주요 UI 파일: `src/main/resources/templates/index.html`
- 표시 항목: 감정별 개수, 카테고리별 개수

### 4.7 결과 다운로드

사용자는 필터링된 피드백 결과를 CSV 파일로 다운로드할 수 있다.

- 처리 엔드포인트: `GET /download`
- 다운로드 파일명: `filtered_feedback.csv`
- 현재 구현: 컨트롤러 필드 `fil_data`에 저장된 마지막 필터 결과를 출력

## 5. Current Architecture

## 5.1 Tech Stack

- Java 17
- Spring Boot 3.5.3
- Spring MVC
- Thymeleaf
- Maven
- OpenCSV
- JUnit / Spring Boot Test

## 5.2 Directory Structure

```text
feedback_analyzer_A02/
├─ README.md
├─ project_purpose.md
├─ pom.xml
├─ docs/
│  └─ PRD.md
├─ src/
│  ├─ main/
│  │  ├─ java/com/example/demo/
│  │  │  ├─ DemoApplication.java
│  │  │  ├─ FeedbackController.java
│  │  │  ├─ Feedback.java
│  │  │  ├─ TextAnalyzer.java
│  │  │  ├─ Filters.java
│  │  │  ├─ Constants.java
│  │  │  ├─ Session.java
│  │  │  ├─ UIComponents.java
│  │  │  ├─ FileHandler.java
│  │  │  └─ Logger.java
│  │  └─ resources/
│  │     ├─ application.properties
│  │     └─ templates/index.html
│  └─ test/java/com/example/demo/
│     └─ DemoApplicationTests.java
└─ target/
```

`target/`는 빌드 산출물이므로 소스 구조 분석에서는 제외한다.

## 5.3 Main Components

| Component | Responsibility |
| --- | --- |
| `DemoApplication.java` | Spring Boot 애플리케이션 시작점 |
| `FeedbackController.java` | 웹 요청 처리, 분석 호출, 업로드, 필터링, 다운로드 담당 |
| `Feedback.java` | 피드백 데이터 모델 |
| `TextAnalyzer.java` | 감정 및 카테고리 키워드 분석 |
| `Filters.java` | 감정/카테고리 기반 피드백 필터링 |
| `Constants.java` | 감정 및 카테고리 키워드 상수 관리 |
| `Session.java` | 현재 피드백 목록과 내부 상태를 static 필드로 관리 |
| `UIComponents.java` | UI에서 사용할 카테고리 목록 제공 |
| `FileHandler.java` | 파일 처리용 서비스지만 현재 실질 사용도 낮음 |
| `Logger.java` | 콘솔 기반 커스텀 로그 출력 |
| `index.html` | 입력, 업로드, 필터, 결과 표시 UI |

## 6. Main User Flows

### 6.1 앱 접속

1. 사용자가 `http://localhost:8080`에 접속한다.
2. `GET /` 요청이 `FeedbackController.index()`로 전달된다.
3. `Session.initSessionStateUgly()`가 전역 상태를 초기화한다.
4. 현재 피드백 목록과 카테고리 목록을 모델에 담아 `index.html`을 렌더링한다.

### 6.2 수동 피드백 분석

1. 사용자가 텍스트 영역에 피드백을 입력한다.
2. `POST /analyze` 요청이 실행된다.
3. 입력값이 비어 있지 않으면 `Feedback` 객체로 추가된다.
4. `TextAnalyzer.sent()`가 감정 분포를 계산한다.
5. `TextAnalyzer.kw()`가 카테고리 분포를 계산한다.
6. 분석 결과가 화면에 표시된다.

### 6.3 CSV 업로드

1. 사용자가 CSV 파일을 선택해 업로드한다.
2. `POST /upload` 요청이 실행된다.
3. 파일이 `C:\tmp` 경로에 임시 저장된다.
4. OpenCSV로 파일을 읽고 첫 번째 컬럼 값을 피드백으로 추가한다.
5. 추가된 피드백 목록이 화면에 표시된다.

### 6.4 필터링 및 다운로드

1. 사용자가 감정 또는 키워드 필터를 선택한다.
2. `POST /filter` 요청이 실행된다.
3. `Filters.fil()`이 조건에 맞는 피드백만 추린다.
4. 필터링된 목록을 다시 분석해 통계를 표시한다.
5. 사용자가 다운로드 버튼을 누르면 `GET /download`가 실행된다.
6. 마지막 필터링 결과가 CSV로 내려간다.

## 7. Known Issues And Risks

### 7.1 Global State

`Session`, `TextAnalyzer`, `FeedbackController.fil_data`가 static 또는 인스턴스 필드 상태에 의존한다.

- 사용자별 세션 분리가 어렵다.
- 동시 요청에서 데이터가 섞일 수 있다.
- 테스트 격리가 어렵다.

### 7.2 Duplicated Business Rules

감정 판정 키워드와 로직이 `Constants.java`, `TextAnalyzer.java`, `Filters.java`에 분산되어 있다.

- 키워드 변경 시 여러 파일을 동시에 수정해야 한다.
- 분석 결과와 필터 결과가 서로 달라질 수 있다.

### 7.3 Controller Overload

`FeedbackController.java`가 요청 처리뿐 아니라 파일 저장, CSV 파싱, 상태 갱신, 분석 호출, 다운로드까지 모두 담당한다.

- 단위 테스트가 어렵다.
- 변경 영향 범위가 커진다.
- 서비스 계층의 책임이 불명확하다.

### 7.4 Weak Domain Model

`Feedback.java`에는 `sentiment`, `category` 필드가 있지만 getter/setter가 없고 실제 분석 결과도 저장되지 않는다.

- 분석 결과가 데이터 모델과 분리되어 추적하기 어렵다.
- 필터링과 다운로드에서 구조화된 데이터를 활용하지 못한다.

### 7.5 CSV Handling Risk

CSV 업로드가 `C:\tmp` 경로와 원본 파일명에 의존한다.

- 운영체제 의존성이 크다.
- 파일명 검증이 부족하다.
- 첫 번째 컬럼 존재 여부 검증이 부족하다.
- 문서상 `text` 컬럼 요구사항과 실제 구현 사이에 검증 로직이 없다.

### 7.6 Low Test Coverage

현재 테스트는 `DemoApplicationTests.contextLoads()`뿐이다.

- 감정 분석 로직 테스트가 없다.
- 카테고리 분석 로직 테스트가 없다.
- 필터링 로직 테스트가 없다.
- CSV 업로드/다운로드 흐름 테스트가 없다.

## 8. Refactoring Direction

### 8.1 Priority 1: 분석 규칙 통합

감정 판정과 카테고리 판정 로직을 하나의 분석 서비스로 통합한다.

권장 방향:

- `AnalysisService` 또는 `FeedbackAnalysisService` 도입
- `Sentiment` enum 도입
- `Category` enum 도입
- 키워드 정의와 판정 로직의 단일 출처화

### 8.2 Priority 2: 전역 상태 제거

`Session`의 static 상태와 컨트롤러의 `fil_data`를 제거한다.

권장 방향:

- 사용자 세션이 필요하면 Spring `HttpSession` 또는 세션 스코프 빈 사용
- 단순 실습용이면 요청 기반 모델 또는 명시적인 저장소 객체 사용
- 다운로드 대상은 필터 조건으로 재계산하거나 사용자별 상태에 저장

### 8.3 Priority 3: Controller 책임 축소

`FeedbackController`에서 비즈니스 로직을 서비스로 이동한다.

권장 분리:

- `FeedbackService`: 피드백 등록/조회
- `AnalysisService`: 감정/카테고리 분석
- `FilterService`: 조건 기반 필터링
- `CsvService`: CSV 업로드/다운로드 처리

### 8.4 Priority 4: 테스트 추가

핵심 로직부터 단위 테스트를 추가한다.

우선순위:

1. 감정 분석 테스트
2. 카테고리 분석 테스트
3. 필터링 테스트
4. CSV 파싱 테스트
5. 컨트롤러 MVC 테스트

### 8.5 Priority 5: UI와 템플릿 정리

`index.html`에 모든 화면 요소가 집중되어 있으므로, 반복되는 표시 구조를 분리하거나 템플릿 fragment를 활용한다.

권장 방향:

- 입력 영역 fragment
- 업로드 영역 fragment
- 필터 영역 fragment
- 결과 영역 fragment
- 메시지 영역 fragment

## 9. Success Criteria

### 9.1 Functional Criteria

- 사용자는 텍스트 피드백을 입력하고 분석 결과를 볼 수 있다.
- 사용자는 CSV 파일을 업로드하고 여러 피드백을 분석할 수 있다.
- 사용자는 감정과 카테고리 기준으로 피드백을 필터링할 수 있다.
- 사용자는 필터링된 결과를 CSV로 다운로드할 수 있다.

### 9.2 Engineering Criteria

- 분석/필터링 로직이 중복 없이 한 곳에서 관리된다.
- 컨트롤러는 요청/응답 조립에 집중한다.
- 전역 mutable 상태가 제거되거나 사용자별로 격리된다.
- 핵심 분석 로직에 단위 테스트가 존재한다.
- CSV 입력 오류가 사용자에게 명확하게 전달된다.
- 새로운 감정 또는 카테고리를 추가할 때 수정 범위가 작다.

## 10. Open Questions

- 피드백 데이터는 사용자 세션 단위로만 유지할 것인가, 파일 또는 DB에 저장할 것인가?
- 감정/카테고리 키워드는 코드 상수로 유지할 것인가, 설정 파일 또는 DB로 관리할 것인가?
- CSV 입력은 반드시 `text` 헤더를 요구할 것인가, 첫 번째 컬럼을 기본값으로 허용할 것인가?
- 분석 결과를 `Feedback` 객체에 저장할 것인가, 별도의 `AnalysisResult`로 분리할 것인가?
- 향후 트렌드 분석 기능은 날짜 컬럼을 포함한 별도 CSV 포맷을 요구할 것인가?
