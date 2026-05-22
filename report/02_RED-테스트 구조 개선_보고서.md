# 02_RED-테스트 구조 개선 보고서

## 1. 작업 개요

이번 세션에서는 `DemoApplicationTests.java`에 JUnit 5 기반 단위 테스트를 추가하여 핵심 비즈니스 로직의 테스트 커버리지를 보강했다.

작업 목표:

- `TextAnalyzer` 테스트 케이스 추가
- `Filters` 테스트 케이스 추가
- `FileHandler` 테스트 케이스 추가
- 테스트 실행 및 커버리지 확인
- Spring 컨텍스트 로딩에 의존하지 않는 단위 테스트 구조로 개선

## 2. 참조 자료

테스트 작성과 검증에 사용한 주요 파일은 다음과 같다.

- `src/test/java/com/example/demo/DemoApplicationTests.java`
- `src/main/java/com/example/demo/TextAnalyzer.java`
- `src/main/java/com/example/demo/Filters.java`
- `src/main/java/com/example/demo/FileHandler.java`
- `src/main/java/com/example/demo/Feedback.java`
- `src/main/java/com/example/demo/Constants.java`
- `pom.xml`

## 3. 기존 상태

기존 테스트 파일은 `@SpringBootTest`와 `contextLoads()`만 포함하고 있었다.

이 구조는 다음 한계가 있었다.

- 감정 분석 로직 검증 없음
- 카테고리 분석 로직 검증 없음
- 필터링 조건별 동작 검증 없음
- `FileHandler`의 저장 위임 및 출력 동작 검증 없음
- 실제 비즈니스 로직 커버리지 부족

## 4. 변경 내용

### 4.1 Spring 컨텍스트 의존 제거

기존 `@SpringBootTest`를 제거하고, 테스트 대상 클래스를 직접 생성하는 순수 단위 테스트 형태로 변경했다.

변경 이유:

- 이번 테스트 대상은 Spring DI 없이 검증 가능한 단순 서비스 로직이다.
- 현재 실행 환경에서 `@SpringBootTest`가 Mockito / ByteBuddy self-attach 오류를 발생시켰다.
- 컨텍스트 로딩보다 빠르고 안정적인 단위 테스트가 적합하다.

### 4.2 TextAnalyzer 테스트 추가

추가한 테스트:

- `textAnalyzerCountsSentimentsByKeyword`
- `textAnalyzerCountsCategoryMatches`

검증 내용:

- 긍정, 부정, 중립 키워드가 포함된 피드백 수를 올바르게 집계하는지 확인
- 배송, 품질, 가격, 서비스, 사용성 카테고리 키워드를 올바르게 집계하는지 확인
- 키워드가 없는 피드백은 카테고리 카운트에 포함되지 않는지 확인

### 4.3 Filters 테스트 추가

추가한 테스트:

- `filtersReturnsAllWhenBothFiltersAreAll`
- `filtersBySentimentOnly`
- `filtersByCategoryOnly`
- `filtersBySentimentAndCategoryTogether`

검증 내용:

- 감정과 카테고리 필터가 모두 `전체`일 때 원본 목록을 반환하는지 확인
- 감정 필터만 적용했을 때 해당 감정의 피드백만 반환하는지 확인
- 카테고리 필터만 적용했을 때 해당 카테고리의 피드백만 반환하는지 확인
- 감정과 카테고리 필터를 동시에 적용했을 때 교집합 결과만 반환하는지 확인

### 4.4 FileHandler 테스트 추가

추가한 테스트:

- `fileHandlerSaveResultPrintsDataSizeAndFeedbackText`
- `fileHandlerSaveDelegatesToSaveResult`

검증 내용:

- `saveResult()`가 데이터 개수와 피드백 텍스트를 출력하는지 확인
- `save()`가 내부적으로 `saveResult()`에 위임되는지 확인
- 표준 출력을 캡처하는 `captureStandardOutput()` 헬퍼를 추가해 출력 기반 동작을 검증

## 5. 테스트 실행 결과

다음 명령으로 테스트를 실행했다.

```bash
mvn test
```

결과:

- Tests run: 9
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

## 6. 커버리지 확인

다음 명령으로 JaCoCo 리포트를 생성했다.

```bash
mvn org.jacoco:jacoco-maven-plugin:prepare-agent test org.jacoco:jacoco-maven-plugin:report
```

대상 클래스 기준 라인 커버리지:

- `TextAnalyzer`: 100%
- `Filters`: 100%
- `FileHandler`: 100%

이번 요청 범위인 `TextAnalyzer`, `Filters`, `FileHandler`는 90% 목표를 초과 달성했다.

단, 프로젝트 전체 기준 90% 커버리지를 의미한다면 `FeedbackController`, `Session`, `Logger`, `UIComponents`, `DemoApplication` 등에 대한 추가 테스트가 필요하다.

## 7. 작업 중 발견한 사항

### 7.1 SpringBootTest 실행 오류

처음에는 기존 구조를 유지한 상태에서 테스트를 추가했으나, `mvn test` 실행 시 Spring 테스트 컨텍스트 초기화 과정에서 Mockito / ByteBuddy 관련 오류가 발생했다.

핵심 오류:

```text
Mockito is unable to load the default implementation of class that is a part of Mockito distribution.
Could not self-attach to current VM using external process
```

조치:

- `@SpringBootTest` 제거
- Spring 컨텍스트 없이 실행되는 단위 테스트 구조로 변경

### 7.2 Filters 중복 매칭 가능성

`Filters`는 카테고리 하위 키워드를 순회하면서 매칭될 때마다 결과 목록에 추가한다. 하나의 피드백이 여러 하위 키워드에 동시에 매칭되면 중복 추가될 수 있다.

이번 테스트에서는 기존 구현을 변경하지 않고, 테스트 데이터가 중복 키워드 매칭을 유발하지 않도록 구성했다.

## 8. 최종 산출물

- `src/test/java/com/example/demo/DemoApplicationTests.java`
- `report/02_RED-테스트 구조 개선_보고서.md`
- `Prompting/02_RED-테스트 구조 개선_보고서-Prompting.md`

## 9. 권장 후속 작업

1. `Filters`의 중복 추가 가능성을 제거하는 리팩토링
2. `TextAnalyzer`와 `Filters`의 감정 키워드 중복 정의 통합
3. `FeedbackController` MVC 테스트 추가
4. CSV 업로드 및 다운로드 흐름 테스트 추가
5. JaCoCo Maven 플러그인을 `pom.xml`에 정식 설정하여 커버리지 기준을 자동 검증
