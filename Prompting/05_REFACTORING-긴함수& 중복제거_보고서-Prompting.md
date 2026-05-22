# 05_REFACTORING-긴함수& 중복제거 보고서 - Prompting

## 1. 목적

이 문서는 이번 세션에서 사용한 사용자 요청과 작업 프롬프트 흐름을 기록한다.

작업 목표:

- `TextAnalyzer.java`의 긴 메서드 추출
- 중복 로직을 공통 private 메서드로 통합
- 기존 분석 동작 보존
- 테스트 실행으로 회귀 여부 확인
- SRP 관점의 클래스 분리 제안 기록

## 2. 사용자 요청 프롬프트

### 2.1 리팩터링 요청

```text
@TextAnalyzer.java 20줄이상 메서드를 추출하고, 중복 로직 을 공통 메서드로 통합해줘. SRP에 따라 클래스 분리 제안도 해줘.
```

의도:

- `TextAnalyzer.java`에서 길어진 분석 메서드를 더 작은 메서드로 분해
- 감정 분석과 카테고리 분석 사이에 반복되는 처리 로직을 공통화
- 단일 책임 원칙 관점에서 향후 클래스 분리 방향 제안

### 2.2 보고서 및 프롬프트 기록 요청

```text
이번세션에서 진행한 내용을 report 폴더의 05_REFACTORING-긴함수& 중복제거_보고서 파일로 내보내주고, 
프롬프트를 포함해서 Prompting 폴더의 05_REFACTORING-긴함수& 중복제거_보고서-Prompting.md 파일로 내보내줘
```

의도:

- 이번 세션의 리팩터링 내용을 보고서로 남김
- 사용자 프롬프트와 작업 흐름을 별도 Prompting 문서로 보관
- `report`와 `Prompting` 폴더에 05번 산출물 생성

## 3. 코드 확인 단계 프롬프트 구조

리팩터링 전 다음 관점으로 코드베이스를 확인했다.

```text
Inspect TextAnalyzer.java and the related test/constant files.

Read:
- src/main/java/com/example/demo/TextAnalyzer.java
- src/test/java/com/example/demo/DemoApplicationTests.java
- src/main/java/com/example/demo/Constants.java

Check:
1. Which public methods are longer than the desired threshold or likely to grow past it.
2. Which logic is duplicated between sentiment analysis and category analysis.
3. Which behavior is already covered by tests.
4. Which constants and keyword maps drive the current behavior.
5. Whether the refactoring can stay behavior-preserving.
```

확인 결과:

- `TextAnalyzer`에는 `analyzeSentiments`와 `analyzeCategoryKeywords` 두 public 분석 메서드가 있었다.
- 두 메서드 모두 텍스트 정규화, 키워드 포함 여부 판단, 카운트 증가 흐름을 직접 수행했다.
- 감정 분석은 긍정 키워드, 부정 키워드, 중립 기본값 순서로 판정했다.
- 카테고리 분석은 `Constants.CATEGORY_KEYWORDS`의 main 키워드를 기준으로 카테고리별 카운트를 증가시켰다.
- `DemoApplicationTests`에는 감정 분석과 카테고리 분석 결과를 검증하는 테스트가 이미 존재했다.

## 4. 관련 로직 추가 확인 프롬프트 구조

키워드 판정 방식이 다른 클래스와 충돌하지 않는지 확인하기 위해 다음 검색 관점을 적용했다.

```text
Search for usage of:
- SENTIMENT_KEYWORDS
- CATEGORY_KEYWORDS
- analyzeSentiments
- analyzeCategoryKeywords
- toLowerCase
- contains

Focus on:
1. Whether the same keyword matching logic exists outside TextAnalyzer.
2. Whether controller code depends on TextAnalyzer method signatures.
3. Whether Filters has related matching behavior that should not be changed in this task.
```

확인 결과:

- `FeedbackController`는 `analyzeSentiments`, `analyzeCategoryKeywords`를 호출하므로 public method signature는 유지해야 했다.
- `Filters`에도 키워드 매칭 로직이 있으나 이번 요청 범위는 `TextAnalyzer.java`로 제한했다.
- 공통화는 `TextAnalyzer` 내부 private 메서드로 먼저 수행하는 것이 안전하다고 판단했다.

## 5. 구현 프롬프트 구조

수정 단계에서는 다음 기준을 적용했다.

```text
Refactor TextAnalyzer.java with behavior preservation.

Keep:
- public method names and return types unchanged
- sentiment priority unchanged
- category matching based on main keywords unchanged
- latestSentimentCounts and latestCategoryCounts behavior unchanged

Extract:
- count initialization
- feedback text normalization
- sentiment detection
- category matching loop
- category main keyword extraction
- shared keyword matching
- count incrementing
```

핵심 구현:

- `initializeCounts(Collection<String> labels)` 추가
- `normalize(Feedback feedback)` 추가
- `detectSentiment(String normalizedFeedbackText)` 추가
- `countMatchedCategories(Map<String, Integer> categoryCounts, String normalizedFeedbackText)` 추가
- `matchesCategory(String normalizedFeedbackText, Map<String, Object> categoryKeywords)` 추가
- `getMainKeywords(Map<String, Object> categoryKeywords)` 추가
- `containsAnyKeyword(String normalizedFeedbackText, List<String> keywords)` 추가
- `incrementCount(Map<String, Integer> counts, String label)` 추가

## 6. 검증 프롬프트 구조

수정 후 다음 검증을 수행했다.

```text
Check IDE diagnostics for:
- src/main/java/com/example/demo/TextAnalyzer.java

Run the test suite:
1. Try the project wrapper if present:
   .\mvnw.cmd test
2. If the wrapper is unavailable, run:
   mvn test

Review the git diff for:
- src/main/java/com/example/demo/TextAnalyzer.java
```

검증 결과:

- `ReadLints` 기준 `TextAnalyzer.java` linter 오류 없음
- `.\mvnw.cmd test`는 저장소에 wrapper 파일이 없어 실행 실패
- `mvn test` 성공
- Tests run: 12
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

참고:

- Maven 컴파일 출력에 `Session.java uses unchecked or unsafe operations` 경고가 표시되었으나 이번 수정 범위 밖의 기존 경고로 판단했다.

## 7. 리팩터링 판단 프롬프트 구조

최종 판단은 다음 기준으로 정리했다.

```text
Evaluate whether the refactoring satisfies the user's request.

Criteria:
- No public API breakage in TextAnalyzer.
- Long methods are reduced to orchestration-level methods.
- Repeated normalization, keyword matching, and count increment logic are shared.
- Existing tests still pass.
- SRP-based class split recommendations are provided.
```

판단:

- `TextAnalyzer`의 public API는 유지되었다.
- public 분석 메서드는 분석 흐름만 읽히도록 축소되었다.
- 중복되던 정규화, 키워드 매칭, 카운트 증가 로직이 공통 private 메서드로 통합되었다.
- 전체 테스트가 통과했다.
- SRP 기준 클래스 분리 방향을 제안했다.

## 8. 보고서 작성용 프롬프트 구조

이번 보고서 작성 시 사용한 정리 기준은 다음과 같다.

```text
Export this session's refactoring work as a report.

Include:
- session overview
- referenced files
- previous state
- extracted methods
- duplicated logic that was unified
- test execution result
- linter result
- SRP-based class split proposal
- remaining issues and follow-up work

Also create a separate prompting document that includes:
- the user's prompts
- the investigation prompt structure
- the implementation prompt structure
- the verification prompt structure
- the refactoring evaluation prompt structure
```

## 9. 작업 결과 요약

생성 및 수정된 파일:

- `src/main/java/com/example/demo/TextAnalyzer.java`
- `report/05_REFACTORING-긴함수& 중복제거_보고서.md`
- `Prompting/05_REFACTORING-긴함수& 중복제거_보고서-Prompting.md`

핵심 판단:

- 이번 작업은 요청된 긴 함수 추출과 중복 제거를 충족한다.
- 테스트 결과는 `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0`이다.
- 다음 단계에서는 `SentimentAnalyzer`, `CategoryAnalyzer`, `KeywordMatcher`, `AnalysisResultStore` 단위의 클래스 분리를 권장한다.
