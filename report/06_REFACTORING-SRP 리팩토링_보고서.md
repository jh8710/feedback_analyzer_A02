# 06_REFACTORING-SRP 리팩토링 보고서

## 1. 작업 개요

이번 세션에서는 `FeedbackController.java`에 섞여 있던 데이터 처리 로직을 `FeedbackService.java`로 분리하여 Controller가 HTTP 요청/응답 처리에 집중하도록 리팩터링했다.

작업 목표:

- `FeedbackController`의 책임을 HTTP 처리와 Model 구성으로 축소
- 피드백 추가, 분석, CSV 업로드, 필터링, 다운로드 데이터 조회 로직을 Service로 이동
- 기존 화면 흐름과 메시지 유지
- 전체 테스트 실행으로 회귀 여부 확인

## 2. 참조 자료

리팩터링과 검증에 사용한 주요 파일은 다음과 같다.

- `src/main/java/com/example/demo/FeedbackController.java`
- `src/main/java/com/example/demo/FeedbackService.java`
- `src/main/java/com/example/demo/TextAnalyzer.java`
- `src/main/java/com/example/demo/Filters.java`
- `src/main/java/com/example/demo/Session.java`
- `src/main/java/com/example/demo/Feedback.java`
- `src/test/java/com/example/demo/DemoApplicationTests.java`
- `pom.xml`

## 3. 기존 상태

기존 `FeedbackController`는 HTTP 요청 처리 외에도 다음 데이터 로직을 직접 수행하고 있었다.

- `Session.initSessionStateUgly()` 호출과 현재 피드백 조회
- 텍스트 입력값을 `Feedback`으로 변환하고 세션에 저장
- `TextAnalyzer`를 직접 호출하여 감정/키워드 분석 수행
- 업로드된 CSV 파일을 임시 파일로 저장한 뒤 직접 파싱
- `Filters`를 직접 호출하고 필터링 결과를 컨트롤러 필드에 저장
- 다운로드 시 컨트롤러 내부 필드인 `fil_data`를 직접 순회
- 로그 레벨 변경을 컨트롤러에서 직접 처리

주요 문제:

- Controller가 HTTP 계층과 비즈니스/데이터 처리 책임을 동시에 가짐
- `TextAnalyzer`, `Filters`, `Session`, CSV 파싱 세부 구현이 Controller에 노출됨
- 필터링 결과 상태가 Controller 필드에 있어 책임 위치가 모호함
- 테스트나 유지보수 시 요청 처리 흐름과 데이터 처리 흐름을 분리해서 보기 어려움

## 4. 변경 내용

### 4.1 `FeedbackService` 추가

새 Service 클래스를 추가하여 피드백 데이터 처리 책임을 이동했다.

추가한 주요 메서드:

- `getFeedbacksForIndex`: 초기 세션 상태 준비 및 현재 피드백 조회
- `addAndAnalyze`: 텍스트 피드백 추가, 세션 저장, 감정/키워드 분석 수행
- `uploadFeedbacks`: 업로드 CSV를 읽어 피드백 목록에 추가
- `filterFeedbacks`: 현재 피드백에 감정/키워드 필터 적용 및 분석 결과 생성
- `getFilteredFeedbacks`: 다운로드에 사용할 필터링 결과 제공
- `updateLogLevel`, `getLogLevel`: 로그 레벨 변경과 조회 위임

### 4.2 분석 결과 DTO 분리

Controller가 Service의 내부 처리 과정을 알 필요가 없도록 `FeedbackService` 내부에 결과 객체를 추가했다.

- `AnalysisResult`
  - 전체 피드백 목록
  - 감정 분석 결과
  - 키워드 분석 결과
  - 결과 존재 여부

- `FilterResult`
  - 필터링된 피드백 목록
  - 감정 분석 결과
  - 키워드 분석 결과
  - 경고 메시지
  - 결과 존재 여부

이 구조를 통해 Controller는 결과 객체의 값을 Model에 담는 역할만 수행한다.

### 4.3 CSV 업로드 로직 이동

기존 Controller는 업로드 파일을 `C:\\tmp` 경로의 임시 파일로 저장한 뒤 `CSVReader`로 다시 읽었다.

변경 후에는 `FeedbackService.uploadFeedbacks()`에서 `MultipartFile.getInputStream()`을 직접 읽어 CSV를 처리한다.

변경 효과:

- Controller에서 파일 저장/파싱 세부 로직 제거
- 불필요한 임시 파일 생성 제거
- CSV 처리 책임을 Service로 이동

### 4.4 필터링 상태 이동

기존 Controller의 `fil_data` 필드를 제거하고, 필터링 결과 상태를 `FeedbackService`의 `filteredFeedbacks`로 이동했다.

변경 전:

- Controller가 필터링 결과를 필드로 보관
- 다운로드 메서드가 Controller 필드를 직접 사용

변경 후:

- Service가 필터링 결과를 보관
- 다운로드 메서드는 `feedbackService.getFilteredFeedbacks()`로 데이터만 조회

### 4.5 Controller 역할 축소

`FeedbackController`는 다음 역할만 남기도록 정리했다.

- 요청 매핑 처리
- 요청 파라미터 수신
- Service 호출
- Service 결과를 `Model`에 추가
- 다운로드 HTTP 헤더와 CSV 응답 작성
- 공통 화면 속성 추가

제거된 직접 의존:

- `TextAnalyzer`
- `Filters`
- `FileHandler`
- `CSVReader`
- `Session`
- 컨트롤러 내부 필터링 데이터 필드

## 5. 최종 구조

리팩터링 후 주요 클래스 책임은 다음과 같다.

### 5.1 `FeedbackController`

역할:

- HTTP endpoint 제공
- 사용자 요청 파라미터 수신
- `FeedbackService` 호출
- View Model 구성
- CSV 다운로드 응답 작성

### 5.2 `FeedbackService`

역할:

- 현재 피드백 목록 조회 및 저장
- 피드백 추가 후 분석 실행
- CSV 업로드 데이터 처리
- 필터링과 필터링 결과 보관
- 로그 레벨 변경 위임
- Controller에 필요한 결과 객체 생성

### 5.3 기존 협력 클래스

- `TextAnalyzer`: 감정/키워드 분석 수행
- `Filters`: 감정/카테고리 필터링 수행
- `Session`: 현재 피드백 목록 보관
- `Logger`: 로그 출력과 로그 레벨 관리
- `UIComponents`: 화면 공통 옵션 제공

## 6. 테스트 실행 결과

다음 명령으로 전체 테스트를 실행했다.

```bash
mvn test
```

결과:

- Tests run: 12
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

참고:

- Maven 컴파일 출력에 `Session.java uses unchecked or unsafe operations` 경고가 표시되었으나, 이번 리팩터링 범위 밖의 기존 경고로 판단했다.

## 7. Linter 확인

수정 및 추가된 파일을 대상으로 IDE 진단을 확인했다.

대상:

- `src/main/java/com/example/demo/FeedbackController.java`
- `src/main/java/com/example/demo/FeedbackService.java`

결과:

- linter 오류 없음

## 8. SRP 기준 평가

이번 리팩터링은 단일 책임 원칙 관점에서 다음 개선을 달성했다.

- Controller는 HTTP 요청/응답 처리와 View Model 구성에 집중한다.
- 피드백 데이터 처리 흐름은 Service로 이동했다.
- CSV 파싱, 세션 업데이트, 분석 호출, 필터링 결과 상태 관리가 Controller에서 제거되었다.
- Controller가 `TextAnalyzer`, `Filters`, `Session`의 세부 동작을 직접 알 필요가 줄었다.

다만 `FeedbackService`는 아직 여러 유스케이스를 한 클래스에서 담당한다. 현재 요청 범위에서는 Controller 책임 분리가 목적이므로 Service 내부 세분화는 후속 작업으로 남겼다.

## 9. 작업 중 발견한 사항

`Session`은 static mutable state를 사용하고 있어 요청 간 데이터 공유 문제가 발생할 수 있다. 실제 웹 애플리케이션의 사용자별 세션 데이터라면 Spring `HttpSession`, session-scoped bean, 또는 별도 저장소로 분리하는 것이 적합하다.

또한 `FeedbackService`는 피드백 추가, CSV 업로드, 필터링, 분석 조합, 로그 레벨 위임을 함께 가진다. 규모가 커지면 다음처럼 더 작은 Service로 나눌 수 있다.

- `FeedbackCommandService`: 피드백 추가 및 업로드 처리
- `FeedbackQueryService`: 현재 피드백과 필터링 결과 조회
- `FeedbackAnalysisService`: 분석 조합 처리
- `FeedbackFilterService`: 필터링 결과 관리
- `LogLevelService`: 로그 레벨 변경 처리

## 10. 최종 산출물

- `src/main/java/com/example/demo/FeedbackController.java`
- `src/main/java/com/example/demo/FeedbackService.java`
- `report/06_REFACTORING-SRP 리팩토링_보고서.md`
- `Prompting/06_REFACTORING-SRP 리팩토링_보고서-Prompting.md`

## 11. 권장 후속 작업

1. `FeedbackService`를 유스케이스별 Service로 추가 분리
2. static `Session` 구조를 실제 사용자 세션 또는 저장소 기반 구조로 교체
3. CSV 업로드 처리에 빈 행, 누락 컬럼, 인코딩 예외 검증 추가
4. `FeedbackController` MVC 테스트 추가
5. `FeedbackService` 단위 테스트 추가
6. 다운로드 CSV 생성 로직을 별도 exporter 클래스로 분리
