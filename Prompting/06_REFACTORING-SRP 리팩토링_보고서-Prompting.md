# 06_REFACTORING-SRP 리팩토링 보고서 - Prompting

## 1. 목적

이 문서는 이번 세션에서 사용한 사용자 요청과 작업 프롬프트 흐름을 기록한다.

작업 목표:

- `FeedbackController.java`의 데이터 처리 책임 분리
- `FeedbackService.java` 신규 생성
- Controller가 HTTP 처리와 Model 구성에 집중하도록 구조 개선
- 테스트 실행으로 회귀 여부 확인
- 작업 내용과 프롬프트를 보고서 산출물로 보관

## 2. 사용자 요청 프롬프트

### 2.1 SRP 리팩터링 요청

```text
@FeedbackController.java 데이터 로직을 Service 클래스로 분리해줘. 
Controller는HTTP 처리만 담당하도록.
```

의도:

- `FeedbackController.java`에 포함된 세션, 분석, 업로드, 필터링 등 데이터 처리 로직을 Service 계층으로 이동
- Controller는 HTTP endpoint, 요청 파라미터 수신, Model 구성, 응답 작성만 담당하도록 책임 축소
- 단일 책임 원칙에 맞게 계층 간 역할을 분리

### 2.2 보고서 및 프롬프트 기록 요청

```text
이번세션에서 진행한 내용을 report 폴더의 06_REFACTORING-SRP 리팩토링_보고서 파일로 내보내주고, 
프롬프트를 포함해서 Prompting 폴더의 06_REFACTORING-SRP 리팩토링_보고서-Prompting.md 파일로 내보내줘
```

의도:

- 이번 세션의 SRP 리팩터링 내용을 보고서로 남김
- 사용자 프롬프트와 작업 흐름을 별도 Prompting 문서로 보관
- `report`와 `Prompting` 폴더에 06번 산출물 생성

## 3. 코드 확인 단계 프롬프트 구조

리팩터링 전 다음 관점으로 코드베이스를 확인했다.

```text
Inspect FeedbackController.java and related service/domain classes.

Read:
- src/main/java/com/example/demo/FeedbackController.java
- src/main/java/com/example/demo/TextAnalyzer.java
- src/main/java/com/example/demo/Filters.java
- src/main/java/com/example/demo/FileHandler.java
- src/main/java/com/example/demo/Logger.java
- src/main/java/com/example/demo/Session.java
- src/main/java/com/example/demo/Feedback.java
- src/test/java/com/example/demo/DemoApplicationTests.java
- pom.xml

Check:
1. Which logic inside FeedbackController is HTTP handling.
2. Which logic is data processing or business workflow.
3. Which collaborators are already Spring services.
4. Whether a new service can preserve existing method signatures and page flow.
5. Which tests should be run after the refactoring.
```

확인 결과:

- `FeedbackController`는 `TextAnalyzer`, `Filters`, `FileHandler`, `UIComponents`, `Logger`에 직접 의존하고 있었다.
- Controller 내부에서 `Session` 조회/수정, 피드백 추가, 분석 실행, CSV 파싱, 필터링, 필터링 결과 저장을 직접 수행했다.
- `TextAnalyzer`, `Filters`, `Logger`, `FileHandler`는 이미 `@Service`로 선언되어 있었다.
- 기존 테스트는 `TextAnalyzer`, `Filters`, `FileHandler`, `Logger`의 주요 동작을 검증하고 있었다.
- 리팩터링은 새로운 `FeedbackService`를 추가하고 Controller의 외부 URL과 View 이름은 유지하는 방식이 적합하다고 판단했다.

## 4. 구현 프롬프트 구조

수정 단계에서는 다음 기준을 적용했다.

```text
Refactor FeedbackController to delegate data logic to a service.

Create:
- FeedbackService as a Spring @Service
- Result objects for analysis and filter use cases

Move from controller to service:
- session initialization and current feedback lookup
- adding text feedback
- updating current feedback session data
- calling TextAnalyzer for sentiment and keyword analysis
- parsing uploaded CSV data
- filtering feedbacks with Filters
- storing filtered feedbacks for download
- log level update delegation

Keep in controller:
- request mappings
- request parameter handling
- model attribute assignment
- common model attributes
- HTTP response headers and output stream for download
- existing view name "index"
```

핵심 구현:

- `FeedbackService` 신규 생성
- `FeedbackService.AnalysisResult` 추가
- `FeedbackService.FilterResult` 추가
- `FeedbackController`에서 `TextAnalyzer`, `Filters`, `FileHandler`, `CSVReader`, `Session`, `fil_data` 직접 사용 제거
- `FeedbackController`가 `FeedbackService`를 주입받아 각 요청 처리에서 호출하도록 변경

## 5. CSV 업로드 리팩터링 프롬프트 구조

CSV 업로드 로직은 다음 기준으로 변경했다.

```text
Move CSV parsing out of the controller.

Prefer reading MultipartFile directly through getInputStream.
Do not keep temporary file creation in the controller.
Preserve:
- skipping the first CSV line
- adding the first column as Feedback text
- updating current feedbacks after upload
- existing success message count
```

변경 결과:

- 기존 `C:\\tmp` 임시 파일 생성 방식 제거
- `MultipartFile.getInputStream()`과 `InputStreamReader`로 CSV 직접 파싱
- CSV 처리 책임을 `FeedbackService.uploadFeedbacks()`로 이동

## 6. 필터링 리팩터링 프롬프트 구조

필터링 로직은 다음 기준으로 변경했다.

```text
Move filtering workflow and filtered state out of FeedbackController.

Service should:
- read current feedbacks
- return a warning when there are no feedbacks
- call Filters.fil for sentiment/category filtering
- return a warning when the filtered result is empty
- analyze filtered feedbacks when results exist
- keep filtered feedbacks for download

Controller should:
- add sentimentResults, keywordResults, filteredFeedbacks to Model when results exist
- add warning to Model when service returns a warning
```

변경 결과:

- Controller 필드 `fil_data` 제거
- 필터링 결과 상태를 `FeedbackService.filteredFeedbacks`로 이동
- Controller는 `FilterResult`를 받아 화면 모델만 구성

## 7. 검증 프롬프트 구조

수정 후 다음 검증을 수행했다.

```text
Check IDE diagnostics for:
- src/main/java/com/example/demo/FeedbackController.java
- src/main/java/com/example/demo/FeedbackService.java

Run the full Maven test suite:

mvn test

Review the git diff for:
- src/main/java/com/example/demo/FeedbackController.java
- src/main/java/com/example/demo/FeedbackService.java
```

검증 결과:

- `ReadLints` 기준 `FeedbackController.java` linter 오류 없음
- `ReadLints` 기준 `FeedbackService.java` linter 오류 없음
- `mvn test` 성공
- Tests run: 12
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

참고:

- Maven 컴파일 출력에 `Session.java uses unchecked or unsafe operations` 경고가 표시되었으나 이번 수정 범위 밖의 기존 경고로 판단했다.
- `mvn test` 실행으로 `target/` 빌드 산출물이 갱신되었다.

## 8. 리팩터링 판단 프롬프트 구조

최종 판단은 다음 기준으로 정리했다.

```text
Evaluate whether the refactoring satisfies the user's SRP request.

Criteria:
- FeedbackController no longer owns data processing workflows.
- Controller handles request mappings, parameters, Model attributes, and HTTP response details.
- FeedbackService owns feedback session workflow, analysis orchestration, upload parsing, filtering, and filtered result state.
- Existing routes and view names remain unchanged.
- Full test suite passes.
- No new linter diagnostics are introduced in edited files.
```

판단:

- `FeedbackController`의 데이터 처리 로직은 `FeedbackService`로 이동했다.
- Controller는 HTTP 처리와 View Model 구성 중심으로 축소되었다.
- 기존 endpoint와 반환 View 이름은 유지되었다.
- 전체 테스트가 통과했다.
- 수정 및 추가 파일에 linter 오류가 없다.

따라서 이번 작업은 요청된 SRP 리팩터링 목적을 충족한다고 판단했다.

## 9. 보고서 작성용 프롬프트 구조

이번 보고서 작성 시 사용한 정리 기준은 다음과 같다.

```text
Export this session's SRP refactoring work as a report.

Include:
- session overview
- referenced files
- previous controller responsibilities
- changes introduced by FeedbackService
- CSV upload logic movement
- filtering state movement
- final class responsibilities
- test execution result
- linter result
- SRP evaluation
- remaining issues and recommended follow-up work

Also create a separate prompting document that includes:
- the user's prompts
- the investigation prompt structure
- the implementation prompt structure
- the CSV upload refactoring prompt structure
- the filtering refactoring prompt structure
- the verification prompt structure
- the refactoring evaluation prompt structure
```

## 10. 작업 결과 요약

생성 및 수정된 파일:

- `src/main/java/com/example/demo/FeedbackController.java`
- `src/main/java/com/example/demo/FeedbackService.java`
- `report/06_REFACTORING-SRP 리팩토링_보고서.md`
- `Prompting/06_REFACTORING-SRP 리팩토링_보고서-Prompting.md`

핵심 판단:

- `FeedbackController`는 HTTP 요청/응답 처리에 집중하도록 축소되었다.
- 피드백 데이터 처리 흐름은 `FeedbackService`로 분리되었다.
- 테스트 결과는 `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`이다.
- 다음 단계에서는 `FeedbackService` 내부 책임을 유스케이스별 Service로 더 나누는 것을 권장한다.
