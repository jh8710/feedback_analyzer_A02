# 05_REFACTORING-긴함수& 중복제거 보고서

## 1. 작업 개요

이번 세션에서는 `TextAnalyzer.java`의 긴 메서드를 분해하고, 감정 분석과 카테고리 분석에 반복되던 공통 로직을 private 메서드로 통합했다.

작업 목표:

- 20줄 이상으로 커질 수 있는 분석 메서드의 책임 분리
- 텍스트 정규화, 키워드 매칭, 카운트 증가 로직 중복 제거
- 기존 감정/카테고리 분석 동작 보존
- SRP 관점의 향후 클래스 분리 방향 제안
- 전체 테스트 실행으로 회귀 여부 확인

## 2. 참조 자료

리팩터링과 검증에 사용한 주요 파일은 다음과 같다.

- `src/main/java/com/example/demo/TextAnalyzer.java`
- `src/main/java/com/example/demo/Constants.java`
- `src/test/java/com/example/demo/DemoApplicationTests.java`
- `src/main/java/com/example/demo/Filters.java`

## 3. 기존 상태

기존 `TextAnalyzer`는 다음 두 public 메서드가 직접 상세 분석 로직을 모두 수행했다.

- `analyzeSentiments`
- `analyzeCategoryKeywords`

주요 문제:

- `feedback.getText().toLowerCase()` 정규화 로직이 반복됨
- `stream().anyMatch(keyword -> text.contains(keyword))` 키워드 포함 여부 판단이 반복됨
- `Map` 카운트 증가 코드가 분석 메서드 내부에 직접 노출됨
- 감정 분석과 카테고리 분석의 흐름 제어, 판정, 집계 책임이 한 메서드에 섞여 있음
- 카테고리 main 키워드 조회 과정에서 unchecked cast가 분석 흐름 중간에 드러남

## 4. 변경 내용

### 4.1 감정 분석 메서드 축소

`analyzeSentiments()`는 감정 카운트 초기화, 피드백 순회, 결과 저장만 담당하도록 축소했다.

추출한 책임:

- 카운트 초기화: `initializeCounts`
- 텍스트 정규화: `normalize`
- 감정 판정: `detectSentiment`
- 키워드 매칭: `containsAnyKeyword`
- 카운트 증가: `incrementCount`

기존 우선순위는 보존했다.

- 긍정 키워드 매칭 시 `긍정`
- 부정 키워드 매칭 시 `부정`
- 둘 다 없으면 `중립`

### 4.2 카테고리 분석 메서드 축소

`analyzeCategoryKeywords()`는 카테고리 카운트 초기화, 피드백 순회, 결과 저장만 담당하도록 축소했다.

추출한 책임:

- 카테고리별 카운트 초기화: `initializeCounts`
- 피드백별 카테고리 매칭 순회: `countMatchedCategories`
- 단일 카테고리 매칭 여부 판단: `matchesCategory`
- category main 키워드 조회: `getMainKeywords`
- 키워드 포함 여부 판단: `containsAnyKeyword`
- 카운트 증가: `incrementCount`

### 4.3 중복 로직 공통화

감정 분석과 카테고리 분석이 함께 사용하던 로직을 공통 private 메서드로 통합했다.

- `normalize`: 피드백 텍스트 소문자 변환
- `containsAnyKeyword`: 키워드 목록 중 하나라도 텍스트에 포함되는지 판단
- `incrementCount`: 특정 라벨의 카운트 증가
- `initializeCounts`: 라벨 목록을 기준으로 초기 카운트 맵 생성

이 변경으로 public 메서드는 분석 흐름을 읽기 쉽게 보여주고, 세부 판정 로직은 이름 있는 메서드에 분리되었다.

## 5. 최종 구조

`TextAnalyzer`의 현재 메서드 역할은 다음과 같다.

- `analyzeSentiments`: 감정 분석 전체 흐름
- `analyzeCategoryKeywords`: 카테고리 분석 전체 흐름
- `initializeCounts`: 분석 결과 맵 초기화
- `normalize`: 피드백 텍스트 정규화
- `detectSentiment`: 감정 키워드 기반 감정 판정
- `countMatchedCategories`: 매칭된 카테고리 카운트 반영
- `matchesCategory`: 카테고리 main 키워드 매칭 여부 판단
- `getMainKeywords`: 카테고리 main 키워드 추출
- `containsAnyKeyword`: 공통 키워드 포함 여부 판단
- `incrementCount`: 공통 카운트 증가 처리

## 6. 테스트 실행 결과

처음에는 다음 명령을 실행했다.

```bash
.\mvnw.cmd test
```

결과:

- 저장소에 `mvnw.cmd`가 없어 실행되지 않음

이후 설치된 Maven으로 다음 명령을 실행했다.

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

수정된 파일을 대상으로 IDE 진단을 확인했다.

대상:

- `src/main/java/com/example/demo/TextAnalyzer.java`

결과:

- linter 오류 없음

## 8. SRP 기준 클래스 분리 제안

이번 작업은 변경 범위를 `TextAnalyzer.java` 내부 리팩터링으로 제한했다. 다음 단계에서는 SRP에 따라 아래처럼 클래스를 분리할 수 있다.

### 8.1 `TextAnalyzer`

역할:

- 분석 전체 흐름 조율
- `SentimentAnalyzer`, `CategoryAnalyzer` 호출
- 최종 분석 결과 반환

### 8.2 `SentimentAnalyzer`

역할:

- 감정 키워드 기반 감정 판정
- 감정별 카운트 집계
- 감정 우선순위 정책 관리

### 8.3 `CategoryAnalyzer`

역할:

- 카테고리 키워드 기반 카테고리 판정
- 카테고리별 카운트 집계
- main/sub 키워드 구조 접근 로직 관리

### 8.4 `KeywordMatcher`

역할:

- 텍스트 정규화
- 키워드 포함 여부 판단
- 감정/카테고리/필터 로직에서 공유 가능한 매칭 규칙 제공

### 8.5 `AnalysisResultStore` 또는 `AnalysisState`

역할:

- 현재 `TextAnalyzer`의 `latestSentimentCounts`, `latestCategoryCounts` 같은 분석 상태 저장 책임 분리
- static mutable state를 서비스 외부로 격리

## 9. 작업 중 발견한 사항

`TextAnalyzer`에는 `latestSentimentCounts`, `latestCategoryCounts` static 필드가 남아 있다.

이번 요청은 긴 함수 추출과 중복 제거가 중심이므로 상태 저장 구조는 변경하지 않았다. 다만 Spring `@Service`에서 static mutable state를 유지하면 요청 간 상태 공유 문제가 생길 수 있으므로 후속 리팩터링 대상으로 분리하는 것이 좋다.

또한 `Constants.CATEGORY_KEYWORDS`는 `Map<String, Map<String, Object>>` 구조라 `getMainKeywords()`에서 unchecked cast가 필요하다. 장기적으로는 명시적인 `CategoryKeywordRule` 같은 타입을 도입하면 타입 안정성을 높일 수 있다.

## 10. 최종 산출물

- `src/main/java/com/example/demo/TextAnalyzer.java`
- `report/05_REFACTORING-긴함수& 중복제거_보고서.md`
- `Prompting/05_REFACTORING-긴함수& 중복제거_보고서-Prompting.md`

## 11. 권장 후속 작업

1. `SentimentAnalyzer`, `CategoryAnalyzer`, `KeywordMatcher`로 클래스 분리
2. `latestSentimentCounts`, `latestCategoryCounts` static 상태 제거 또는 별도 상태 저장 컴포넌트로 이동
3. `Constants.CATEGORY_KEYWORDS`의 raw `Object` 기반 중첩 맵을 명시적 타입으로 교체
4. `Filters`와 `TextAnalyzer`가 사용하는 감정/카테고리 키워드 판정 로직 통합
5. 클래스 분리 후 감정 분석, 카테고리 분석, 키워드 매칭 단위 테스트를 더 작은 단위로 재구성
