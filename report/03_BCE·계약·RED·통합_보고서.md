# 03. BCE·계약·RED·통합 보고서

## 1. 세션 목적

이번 세션의 목적은 `feedback_analyzer_A02` 프로젝트를 대상으로 BCE 관점의 설계 기준, 외부 계약, RED 단계 테스트 목록, 통합 검증 계획을 정리하는 것이다.

사용자 제약은 다음과 같았다.

- 구현 코드 작성 금지
- 설계, 계약, 테스트 목록, 통합 계획만 작성
- Java와 JUnit 5를 전제로 작성
- Dual-Track 관점에서 UI/경계와 Domain을 분리
- 모든 규칙은 테스트로 검증 가능해야 함

## 2. 확인한 프로젝트 상태

프로젝트는 Spring Boot + Thymeleaf 기반 고객 피드백 분석 시스템이다.

현재 구조에서 확인한 핵심 특징은 다음과 같다.

- `FeedbackController`가 입력 처리, CSV 업로드, 분석 호출, 필터링, 다운로드 응답을 함께 담당한다.
- `TextAnalyzer`가 키워드 기반 감정 분석과 카테고리 집계를 수행한다.
- `Filters`가 별도 기준으로 필터링을 수행하여 분석 기준과 불일치할 수 있다.
- `Session`은 static 전역 상태를 사용한다.
- `FileHandler`는 실제 저장 책임이 명확하지 않다.
- `DemoApplicationTests`는 Spring Context 로드만 확인한다.

따라서 현재 코드의 주요 개선 방향은 Boundary, Control, Entity, Data 책임을 분리하고, 테스트 가능한 계약을 먼저 정의하는 것이다.

## 3. BCE 설계 관점

### 3.1 Entity / Domain

초기 BCE 설계 요청에서는 `ConversionRule`, `UnitRegistry`, `Converter`를 기준으로 단위 변환 도메인을 분리했다.

`ConversionRule`의 단일 책임은 단위명과 meter 허브 기준 비율을 표현하고, 비율 유효성을 보장하는 것이다.

`UnitRegistry`의 단일 책임은 등록된 단위 목록을 관리하고, 중복 등록과 미등록 단위 조회를 통제하는 것이다.

`Converter`의 단일 책임은 `source -> meter -> target` 변환 공식을 적용하는 것이다.

도메인 invariant는 다음과 같이 정리했다.

- `meter`는 기본 허브 단위이며 ratio는 항상 `1`이다.
- ratio는 항상 `0`보다 커야 한다.
- 정규화된 단위명은 빈 문자열일 수 없다.
- 정규화 규칙은 앞뒤 공백 제거와 소문자 변환이다.
- 정규화된 단위명은 registry 안에서 중복될 수 없다.
- `meter`는 삭제, ratio 변경, 중복 등록할 수 없다.
- 변환 입력값은 `0` 이상이어야 한다.
- 변환 공식은 `sourceValue * sourceRatioToMeter / targetRatioToMeter`만 허용한다.

### 3.2 Boundary

Boundary는 사용자 입력을 검증하고 Domain 호출에 필요한 요청 모델로 변환한다.

대표 시나리오는 다음과 같다.

1. 사용자가 `단위:값` 형식의 문자열을 입력한다.
2. Boundary가 형식과 음수 여부를 검증한다.
3. Boundary가 값을 `BigDecimal`로 변환한다.
4. Boundary가 target unit을 검증한다.
5. Domain `Converter`를 호출한다.
6. 성공 또는 실패 schema를 반환한다.

외부 계약은 다음 세 가지로 나눴다.

- Input schema: `input`, `targetUnit`
- Output schema: `sourceUnit`, `targetUnit`, `sourceValue`, `convertedValue`, `formula`
- Error schema: `code`, `message`, `field`, `rejectedValue`

에러 메시지는 문자열 완전 일치로 테스트 가능해야 한다.

### 3.3 Data

Data 설계의 목적은 설정 외부화다.

단위 ratio 설정은 코드에 고정하지 않고 JSON 또는 YAML에서 로드한다.

저장소 계약 이름은 다음 두 가지로 제한했다.

- `loadRatios`
- `saveRatios`

비교 결과 운영 설정은 YAML 파일을 추천하고, 테스트에서는 InMemory 대역을 사용하는 방향을 제안했다.

### 3.4 Integration

의존성 방향은 다음과 같이 정리했다.

```text
Boundary -> Control(Application Service) -> Domain
Control -> Data Port(interface)
Data Adapter -> Data Port(interface)
```

검증 규칙은 다음과 같다.

- Domain은 Spring MVC, JSON, YAML, 파일 API를 알지 않는다.
- Boundary는 변환 공식을 알지 않는다.
- Data Adapter는 변환 계산을 하지 않는다.
- Control은 흐름만 조립하고 Domain invariant를 재구현하지 않는다.

## 4. Feedback Analyzer RED 테스트 목록

이후 요청에서는 JUnit 5를 가정하고 고객 피드백 분석 기능에 대한 RED 단계 테스트 제목을 정리했다.

### 4.1 텍스트 피드백 입력

1. `manualInput_shouldRejectBlankText`
   - Invariant: `FeedbackTextMustNotBeBlank`
2. `manualInput_shouldTrimLeadingAndTrailingWhitespace`
   - Invariant: `FeedbackTextIsStoredNormalized`
3. `manualInput_shouldPreserveMultilineText`
   - Invariant: `FeedbackTextMayContainLineBreaks`
4. `manualInput_shouldAppendNewFeedbackWithoutClearingExistingFeedbacks`
   - Invariant: `NewFeedbackDoesNotDeleteExistingSessionData`
5. `csvUpload_shouldRejectEmptyFile`
   - Invariant: `CsvFileMustContainAtLeastOneDataRow`
6. `csvUpload_shouldRejectFileWithoutTextColumn`
   - Invariant: `CsvInputRequiresTextColumn`
7. `csvUpload_shouldImportOnlyNonBlankTextRows`
   - Invariant: `BlankCsvRowsAreNotStored`
8. `csvUpload_shouldRejectRowsExceedingMaximumTextLength`
   - Invariant: `FeedbackTextLengthLimitIsEnforced`

### 4.2 키워드 기반 피드백 분류

9. `keywordClassification_shouldClassifyDeliveryFeedbackAsDelivery`
   - Invariant: `DeliveryKeywordsMapToDeliveryCategory`
10. `keywordClassification_shouldClassifyQualityFeedbackAsQuality`
    - Invariant: `QualityKeywordsMapToQualityCategory`
11. `keywordClassification_shouldClassifyPriceFeedbackAsPrice`
    - Invariant: `PriceKeywordsMapToPriceCategory`
12. `keywordClassification_shouldClassifyServiceFeedbackAsService`
    - Invariant: `ServiceKeywordsMapToServiceCategory`
13. `keywordClassification_shouldClassifyUsabilityFeedbackAsUsability`
    - Invariant: `UsabilityKeywordsMapToUsabilityCategory`
14. `keywordClassification_shouldReturnUnknownWhenNoCategoryKeywordMatches`
    - Invariant: `UnmatchedFeedbackHasExplicitUnknownCategory`
15. `keywordClassification_shouldUseSingleSharedKeywordDictionary`
    - Invariant: `ClassificationAndFilteringUseSameKeywordSource`

### 4.3 감정 분석

16. `sentimentAnalysis_shouldClassifyPositiveKeywordAsPositive`
    - Invariant: `PositiveKeywordsMapToPositiveSentiment`
17. `sentimentAnalysis_shouldClassifyNegativeKeywordAsNegative`
    - Invariant: `NegativeKeywordsMapToNegativeSentiment`
18. `sentimentAnalysis_shouldClassifyNoSentimentKeywordAsNeutral`
    - Invariant: `NoSentimentMatchDefaultsToNeutral`
19. `sentimentAnalysis_shouldPreferNegativeWhenPositiveAndNegativeKeywordsCoexist`
    - Invariant: `MixedSentimentResolutionRuleIsDeterministic`
20. `sentimentAnalysis_shouldBeCaseInsensitive`
    - Invariant: `SentimentMatchingIgnoresCase`

### 4.4 피드백 필터링 및 검색

21. `filtering_shouldReturnOnlyFeedbacksMatchingSelectedSentiment`
    - Invariant: `SentimentFilterDoesNotLeakOtherSentiments`
22. `filtering_shouldReturnOnlyFeedbacksMatchingSelectedCategory`
    - Invariant: `CategoryFilterDoesNotLeakOtherCategories`
23. `filtering_shouldCombineSentimentAndCategoryWithAndCondition`
    - Invariant: `CombinedFiltersUseLogicalAnd`
24. `filtering_shouldReturnAllFeedbacksWhenAllFiltersAreSelected`
    - Invariant: `AllFilterMeansNoRestriction`
25. `search_shouldReturnFeedbacksContainingQueryText`
    - Invariant: `SearchMatchesFeedbackText`
26. `search_shouldBeCaseInsensitive`
    - Invariant: `SearchIgnoresCase`
27. `search_shouldReturnEmptyResultWhenNoFeedbackMatches`
    - Invariant: `NoSearchMatchReturnsEmptyList`

### 4.5 분석 결과 시각화

28. `visualization_shouldExposeSentimentCountsForPositiveNeutralNegative`
    - Invariant: `SentimentChartAlwaysHasThreeBuckets`
29. `visualization_shouldExposeCategoryCountsForAllConfiguredCategories`
    - Invariant: `CategoryChartUsesConfiguredCategorySet`
30. `visualization_shouldReturnZeroCountsWhenNoFeedbackExists`
    - Invariant: `EmptyDatasetProducesZeroCounts`
31. `visualization_shouldNotMutateFeedbackDataWhileBuildingChartModel`
    - Invariant: `ChartModelCreationIsReadOnly`

### 4.6 결과 CSV 다운로드

32. `csvDownload_shouldIncludeUtf8Bom`
    - Invariant: `DownloadedCsvIsExcelReadableUtf8`
33. `csvDownload_shouldWriteHeaderRow`
    - Invariant: `DownloadedCsvHasStableHeader`
34. `csvDownload_shouldExportOnlyCurrentlyFilteredFeedbacks`
    - Invariant: `DownloadUsesCurrentFilterResult`
35. `csvDownload_shouldEscapeCommaQuoteAndLineBreakInText`
    - Invariant: `CsvOutputEscapesSpecialCharacters`
36. `csvDownload_shouldReturnEmptyCsvWithHeaderWhenFilteredResultIsEmpty`
    - Invariant: `EmptyDownloadStillHasSchema`
37. `csvDownload_shouldUseDeterministicFileName`
    - Invariant: `DownloadFileNameIsStable`
38. `csvDownload_shouldNotExposeFeedbackFromOtherUserSession`
    - Invariant: `DownloadDataIsSessionScoped`

## 5. 통합 검증 기준

통합 테스트는 정상 흐름과 실패 흐름을 분리한다.

정상 흐름은 다음 기능을 보호한다.

- 수동 입력 후 감정 분석과 카테고리 집계가 생성된다.
- CSV 업로드 후 동일한 분석 흐름을 통과한다.
- 필터링 결과만 CSV 다운로드에 사용된다.

실패 흐름은 다음 기능을 보호한다.

- 빈 수동 입력은 저장되지 않는다.
- text 컬럼이 없는 CSV는 거부된다.
- 없는 카테고리 또는 감정 필터는 결과에 영향을 주지 않거나 명시적으로 실패한다.
- 특수 문자가 포함된 피드백은 CSV에서 깨지지 않는다.
- 다른 사용자 세션의 피드백은 다운로드되지 않는다.

## 6. 회귀 보호 규칙

회귀 보호 규칙은 다음과 같다.

- 입력 검증 규칙은 Boundary 테스트로 고정한다.
- 감정 기준과 필터 기준은 하나의 사전에서 파생되어야 한다.
- 카테고리 목록은 분석, 필터, 화면 표시에서 동일해야 한다.
- CSV 다운로드 header와 encoding은 exact assertion으로 검증한다.
- 전역 상태 또는 공유 필드로 인해 사용자별 데이터가 섞이면 테스트가 실패해야 한다.
- 시각화 모델 생성은 원본 피드백 목록을 변경하면 안 된다.

## 7. 커버리지 목표

권장 커버리지 목표는 다음과 같다.

- Domain 또는 분석 정책: line 90% 이상, branch 95% 이상
- Boundary 또는 Controller 계약: line 85% 이상, branch 90% 이상
- Data 또는 CSV 처리: line 80% 이상, branch 85% 이상
- 통합 테스트: 정상 2건 이상, 실패 3건 이상

## 8. Traceability Matrix

| 요구사항 | 보호 대상 | 테스트 제목 |
|---|---|---|
| 수동 입력 검증 | 빈 문자열 저장 금지 | `manualInput_shouldRejectBlankText` |
| 수동 입력 정규화 | 앞뒤 공백 제거 | `manualInput_shouldTrimLeadingAndTrailingWhitespace` |
| CSV 입력 검증 | text 컬럼 필수 | `csvUpload_shouldRejectFileWithoutTextColumn` |
| CSV 입력 정제 | blank row 제외 | `csvUpload_shouldImportOnlyNonBlankTextRows` |
| 카테고리 분류 | 배송 키워드 기준 | `keywordClassification_shouldClassifyDeliveryFeedbackAsDelivery` |
| 카테고리 분류 | 공통 키워드 사전 | `keywordClassification_shouldUseSingleSharedKeywordDictionary` |
| 감정 분석 | 긍정 기준 | `sentimentAnalysis_shouldClassifyPositiveKeywordAsPositive` |
| 감정 분석 | 중립 기본값 | `sentimentAnalysis_shouldClassifyNoSentimentKeywordAsNeutral` |
| 필터링 | 감정 필터 누수 방지 | `filtering_shouldReturnOnlyFeedbacksMatchingSelectedSentiment` |
| 필터링 | AND 조건 | `filtering_shouldCombineSentimentAndCategoryWithAndCondition` |
| 검색 | 대소문자 무시 | `search_shouldBeCaseInsensitive` |
| 시각화 | 감정 bucket 고정 | `visualization_shouldExposeSentimentCountsForPositiveNeutralNegative` |
| 시각화 | 원본 데이터 불변 | `visualization_shouldNotMutateFeedbackDataWhileBuildingChartModel` |
| CSV 다운로드 | UTF-8 BOM | `csvDownload_shouldIncludeUtf8Bom` |
| CSV 다운로드 | 특수 문자 escape | `csvDownload_shouldEscapeCommaQuoteAndLineBreakInText` |
| 세션 격리 | 다른 사용자 데이터 차단 | `csvDownload_shouldNotExposeFeedbackFromOtherUserSession` |

## 9. 다음 단계

다음 단계는 구현이 아니라 RED 테스트 파일의 골격을 작성하는 것이다.

테스트 작성 순서는 다음을 권장한다.

1. 입력 검증 테스트
2. 감정 분석 테스트
3. 키워드 분류 테스트
4. 필터링 테스트
5. CSV 다운로드 테스트
6. 통합 테스트

이 순서는 현재 코드의 위험도가 높은 입력, 기준 불일치, 전역 상태, 출력 포맷 문제를 먼저 드러낸다.
