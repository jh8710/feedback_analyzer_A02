# 03_GREEN-버그수정 3건 점검 보고서

## 1. 작업 개요

이번 세션에서는 사용자 요청에 따라 버그 및 UI 개선 3건을 수정하고, 수정 이후 테스트가 Green 단계를 충족하는지 점검했다.

작업 목표:

- `Filters.java`의 `중립` 감정 필터 버그 수정
- `index.html`의 텍스트 입력창을 multiline 입력에 적합하게 보강
- `Logger.java`의 warning/error 로그 레벨을 UI에서 제어하도록 수정
- 버그 수정 후 회귀 테스트 추가
- `mvn test` 실행 결과 기준 Green 단계 충족 여부 확인

## 2. 참조 자료

수정 및 검증에 사용한 주요 파일은 다음과 같다.

- `src/main/java/com/example/demo/Filters.java`
- `src/main/java/com/example/demo/Logger.java`
- `src/main/java/com/example/demo/FeedbackController.java`
- `src/main/resources/templates/index.html`
- `src/test/java/com/example/demo/DemoApplicationTests.java`
- `pom.xml`

## 3. 기존 상태

기존 구현에는 다음 문제가 있었다.

- `Filters.java`에서 `중립` 필터 판정 시 `괜찮` 같은 키워드가 긍정 키워드와 중복되어 긍정으로 먼저 분류될 수 있었다.
- 피드백 입력창은 `textarea`였지만 multiline 입력 의도가 UI 속성으로 명확히 드러나지 않았다.
- `Logger.java`는 warning/error 로그를 UI에서 선택적으로 제어할 수 있는 설정 흐름이 없었다.
- 기존 테스트에는 이번 버그 수정 사항을 직접 검증하는 회귀 테스트가 없었다.

## 4. 변경 내용

### 4.1 `Filters.java` 중립 필터 버그 수정

감정 판정 로직을 `getSentiment()` 메서드로 분리했다.

수정 내용:

- `중립` 키워드를 먼저 판정
- 이후 `부정`, `긍정` 순서로 판정
- 어떤 감정 키워드에도 매칭되지 않으면 기존 동작과 동일하게 `중립`으로 처리

수정 효과:

- `서비스가 괜찮습니다`처럼 `중립` 키워드를 포함한 문장이 `긍정` 필터로 잘못 분류되는 문제를 방지
- 중립 필터 결과가 사용자 기대와 일치하도록 개선

### 4.2 `index.html` multiline 입력창 보강

피드백 입력창을 multiline 입력에 적합하게 보강했다.

수정 내용:

- `textarea`에 `rows="5"` 추가
- `wrap="soft"` 추가
- placeholder를 여러 줄 입력 안내 문구로 변경
- CSS에 `min-height`, `resize: vertical`, `box-sizing: border-box` 적용

수정 효과:

- 여러 줄 피드백 입력이 명확해짐
- 사용자가 입력창 높이를 세로로 조절할 수 있음
- 입력창 너비 계산이 안정화됨

### 4.3 `Logger.java` 로그 레벨 제어 추가

warning/error 로그 레벨을 UI에서 제어할 수 있도록 수정했다.

수정 내용:

- `Logger.LogLevel` enum 추가
- 기본 로그 레벨을 `WARNING`으로 설정
- `setLogLevel(String level)` 추가
- `getLogLevel()` 추가
- `logWarning()`은 현재 로그 레벨이 `WARNING`일 때만 출력
- `logError()`는 로그 레벨과 관계없이 출력 유지

수정 효과:

- UI에서 `Warning 이상` 또는 `Error만`을 선택 가능
- `Error만` 선택 시 warning 로그는 억제되고 error 로그는 계속 출력됨

### 4.4 `FeedbackController.java` UI 연결

로그 레벨 설정 UI와 서버 로직을 연결했다.

수정 내용:

- `/log-level` POST 엔드포인트 추가
- 로그 레벨 변경 성공/실패 메시지 처리
- 공통 모델 속성 설정을 `addCommonModelAttributes()`로 분리
- 모든 주요 화면 반환 경로에서 `categories`와 `logLevel`을 모델에 포함

수정 효과:

- 화면에서 선택한 로그 레벨이 서버의 `Logger` 설정에 반영됨
- 화면 재렌더링 시 현재 로그 레벨 선택 상태가 유지됨

## 5. 추가 테스트

`DemoApplicationTests.java`에 다음 회귀 테스트를 추가했다.

- `filtersNeutralKeywordAsNeutralWhenItOverlapsPositiveKeyword`
- `loggerSuppressesWarningsWhenLogLevelIsError`
- `loggerKeepsErrorsEnabledWhenLogLevelIsError`

검증 내용:

- `괜찮습니다`가 긍정 키워드와 겹치더라도 `중립` 필터 결과로 반환되는지 확인
- 로그 레벨이 `error`일 때 warning 로그가 표준 출력에 출력되지 않는지 확인
- 로그 레벨이 `error`일 때도 error 로그가 표준 에러에 출력되는지 확인
- 테스트 후 로그 레벨을 `warning`으로 복구해 다른 테스트에 영향을 주지 않도록 처리

## 6. 테스트 실행 결과

다음 명령으로 테스트를 실행했다.

```bash
mvn test
```

결과:

- Tests run: 12
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

IDE 진단 확인 결과:

- 수정 파일 linter 오류 없음

## 7. Green 단계 충족 여부

이번 버그 수정 작업은 Green 단계를 충족한다.

판단 근거:

- 요청된 3개 수정 사항이 모두 코드에 반영됨
- 각 버그 수정에 대한 회귀 테스트가 추가됨
- 전체 테스트가 실패 없이 통과함
- 수정 파일 기준 linter 오류가 없음

주의사항:

- Maven 컴파일 출력에 `Session.java uses unchecked or unsafe operations` 경고가 남아 있으나, 이번 수정 범위 밖의 기존 경고이며 테스트 실패 요인은 아니다.
- 이번 세션에서는 JaCoCo 커버리지 리포트를 새로 생성하지 않았고, Green 판단은 `mvn test` 성공과 회귀 테스트 통과를 기준으로 했다.

## 8. 최종 산출물

생성 및 수정된 파일:

- `src/main/java/com/example/demo/Filters.java`
- `src/main/java/com/example/demo/Logger.java`
- `src/main/java/com/example/demo/FeedbackController.java`
- `src/main/resources/templates/index.html`
- `src/test/java/com/example/demo/DemoApplicationTests.java`
- `report/03_GREEN-버그수정 3건 점검_보고서.md`
- `Prompting/03_GREEN-버그수정 3건 점검_보고서-Prompting.md`

## 9. 권장 후속 작업

1. `TextAnalyzer`와 `Filters`의 감정 키워드 정의를 하나로 통합
2. `Logger`의 static 상태를 테스트와 운영 코드에서 더 안전하게 다루도록 인스턴스 기반 구조 검토
3. `/log-level` 엔드포인트에 대한 MVC 테스트 추가
4. `Session.java`의 unchecked 경고 정리
5. Green 이후 Refactor 단계에서 중복 모델 속성 설정과 필터 로직을 추가 정리
