# 03_GREEN-버그수정 3건 점검 보고서 - Prompting

## 1. 목적

이 문서는 이번 세션에서 사용한 사용자 요청과 작업 프롬프트 흐름을 기록한다.

작업 목표:

- 버그 수정 3건 수행
- 수정 이후 회귀 테스트 추가
- `mvn test` 기준 Green 단계 충족 여부 확인
- 작업 내용과 프롬프트를 보고서 산출물로 보관

## 2. 사용자 요청 프롬프트

### 2.1 버그 수정 요청

```text
@Filters.java '중립' 필터버그를수정해줘.
@index.html 텍스트입력창을multiline으로수정.
@Logger.java 로그레벨(warning/error)을UI에서제어하도록수정.
```

의도:

- `Filters.java`에서 `중립` 감정 필터가 올바르게 동작하도록 수정
- `index.html`의 피드백 입력 UI를 여러 줄 입력에 적합하게 수정
- `Logger.java`의 warning/error 로그 레벨을 화면에서 선택하고 적용할 수 있도록 연결

### 2.2 Green 단계 충족 여부 확인 요청

```text
버그수정 이후 테스트 결과는 Green 을 충족하나
```

이어진 확인 요청:

```text
보고서 작성 전 점검을 해보려고 하는데 green단계를 충족하는지 물어본거야
```

의도:

- 보고서 작성 전에 현재 수정 상태가 TDD Green 단계 기준을 만족하는지 확인
- 단순 테스트 통과 여부뿐 아니라 요청된 버그 수정과 회귀 테스트가 반영되었는지 점검

### 2.3 보고서 및 프롬프트 기록 요청

```text
이번세션에서 진행한 내용을 report 폴더의 03_GREEN-버그수정 3건 점검_보고서 파일로 내보내주고, 
프롬프트를 포함해서 Prompting 폴더의 03_GREEN-버그수정 3건 점검_보고서-Prompting.md 파일로 내보내줘
```

의도:

- 이번 세션의 버그 수정 및 Green 점검 내용을 보고서로 남김
- 사용자 프롬프트와 작업 흐름을 별도 Prompting 문서로 보관
- `report`와 `Prompting` 폴더에 03번 산출물 생성

## 3. 코드 확인 단계 프롬프트 구조

버그 수정 전 다음 관점으로 코드베이스를 확인했다.

```text
Inspect the requested files and the related controller flow.

Read:
- src/main/java/com/example/demo/Filters.java
- src/main/java/com/example/demo/Logger.java
- src/main/resources/templates/index.html
- src/main/java/com/example/demo/FeedbackController.java
- src/test/java/com/example/demo/DemoApplicationTests.java

Check:
1. How sentiment filtering currently determines positive, neutral, and negative feedback.
2. Whether the text input is already single-line or multiline.
3. How warning and error logs are currently emitted.
4. Whether the UI has a path to configure logger behavior.
5. What tests already exist and where regression tests should be added.
```

확인 결과:

- `Filters.java`는 감정 판정 시 긍정 키워드를 먼저 확인했다.
- `괜찮` 키워드가 긍정과 중립 키워드 양쪽에 있어 `중립` 필터에서 누락될 수 있었다.
- `index.html`의 입력창은 `textarea`였지만 multiline 입력 의도를 더 명확히 할 여지가 있었다.
- `Logger.java`는 debug 모드만 가지고 있었고 warning/error 레벨 제어는 없었다.
- `FeedbackController.java`에는 로그 레벨 설정 엔드포인트가 없었다.
- 기존 테스트에는 이번 버그 수정 내용을 직접 검증하는 테스트가 없었다.

## 4. 구현 프롬프트 구조

수정 단계에서는 다음 기준을 적용했다.

```text
Implement the three requested fixes with minimal scope.

For Filters.java:
- Extract sentiment detection into a helper method.
- Ensure neutral keywords are checked before positive keywords when filtering.
- Preserve the existing fallback behavior where unmatched feedback is considered neutral.

For index.html:
- Keep the input as textarea.
- Add explicit multiline attributes and improve resize behavior.

For Logger.java and the UI:
- Add a warning/error log level.
- Suppress warning logs when the level is error.
- Keep error logs enabled.
- Add a simple UI select and a controller endpoint to update the level.
- Keep model attributes available after each request.
```

핵심 구현:

- `Filters.getSentiment()` 추가
- `Logger.LogLevel` enum 추가
- `Logger.setLogLevel()` / `Logger.getLogLevel()` 추가
- `FeedbackController.updateLogLevel()` 추가
- `FeedbackController.addCommonModelAttributes()` 추가
- `index.html`에 로그 설정 섹션 추가

## 5. 테스트 추가 프롬프트 구조

회귀 테스트 작성 시 다음 기준을 적용했다.

```text
Add focused regression tests in DemoApplicationTests.java.

Cover:
1. Neutral filtering when a neutral keyword overlaps with a positive keyword.
2. Warning log suppression when log level is error.
3. Error log output still working when log level is error.

Capture System.out and System.err with ByteArrayOutputStream.
Restore global Logger state in finally blocks so tests do not leak state.
```

추가된 테스트:

- `filtersNeutralKeywordAsNeutralWhenItOverlapsPositiveKeyword`
- `loggerSuppressesWarningsWhenLogLevelIsError`
- `loggerKeepsErrorsEnabledWhenLogLevelIsError`

## 6. 검증 프롬프트 구조

수정 후 다음 검증을 수행했다.

```text
Run the full Maven test suite:

mvn test

Then check IDE diagnostics for the recently edited files:
- Filters.java
- Logger.java
- FeedbackController.java
- index.html
- DemoApplicationTests.java

Review the relevant git diff to make sure the changes match the requested scope.
```

검증 결과:

- `mvn test` 성공
- Tests run: 12
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS
- 수정 파일 linter 오류 없음

참고:

- Maven 컴파일 출력에 `Session.java uses unchecked or unsafe operations` 경고가 표시되었으나 이번 수정 범위 밖의 기존 경고로 판단했다.

## 7. Green 점검 판단 프롬프트 구조

사용자의 Green 단계 충족 여부 질문에는 다음 기준으로 답변했다.

```text
Evaluate whether this work satisfies the Green phase.

Green criteria:
- The requested behavior changes are implemented.
- Regression tests cover the corrected behavior.
- The full test suite passes.
- No new linter diagnostics are introduced in edited files.

Mention any residual warning that is outside the current scope.
```

판단:

- 요청된 3개 수정 사항이 모두 반영됨
- 회귀 테스트 3개가 추가됨
- 전체 테스트가 통과함
- 수정 파일 linter 오류가 없음

따라서 이번 세션의 버그 수정은 Green 단계를 충족한다고 판단했다.

## 8. 보고서 작성용 프롬프트 구조

이번 보고서 작성 시 사용한 정리 기준은 다음과 같다.

```text
Export this session's Green bug-fix verification work as a report.

Include:
- session overview
- referenced files
- previous state
- changes for each of the three requested fixes
- regression tests added
- test execution result
- Green phase evaluation
- residual warnings or caveats
- final deliverables
- recommended next steps

Also create a separate prompting document that includes:
- the user's prompts
- the investigation prompt structure
- the implementation prompt structure
- the testing prompt structure
- the Green evaluation prompt structure
```

## 9. 작업 결과 요약

생성 및 수정된 파일:

- `src/main/java/com/example/demo/Filters.java`
- `src/main/java/com/example/demo/Logger.java`
- `src/main/java/com/example/demo/FeedbackController.java`
- `src/main/resources/templates/index.html`
- `src/test/java/com/example/demo/DemoApplicationTests.java`
- `report/03_GREEN-버그수정 3건 점검_보고서.md`
- `Prompting/03_GREEN-버그수정 3건 점검_보고서-Prompting.md`

핵심 판단:

- 이번 작업은 TDD Green 단계 기준을 충족한다.
- 테스트 결과는 `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`이다.
- `Session.java`의 unchecked 경고는 이번 작업 범위 밖의 잔여 경고로 기록한다.
