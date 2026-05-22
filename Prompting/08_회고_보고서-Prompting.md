# 08_회고 보고서 - Prompting

## 1. 목적

이 문서는 이번 세션에서 사용한 사용자 요청과 작업 프롬프트 흐름을 기록한다.

작업 목표:

- 리팩토링 전후 비교 회고 문서 작성
- Before/After 코드 예시 포함
- `docs/retrospective.md` 산출물 생성
- 회고 문서 작성 과정과 프롬프트를 별도 Prompting 문서로 보관

## 2. 사용자 요청 프롬프트

### 2.1 리팩토링 전후 비교 보고서 요청

```text
@Codebase 리팩토링전후비교보고서를Markdown으로작성해줘. 
Before/After 코드포함.
이 내용은 docs/retrospective.md 파일로 내보내줘
```

의도:

- 코드베이스 전체 리팩토링 흐름을 하나의 회고 문서로 정리
- 설명만이 아니라 Before/After 코드 예시를 포함
- 최종 결과를 `docs/retrospective.md`로 저장

### 2.2 회고 보고서 및 프롬프트 기록 요청

```text
이번세션에서 진행한 내용을 report 폴더의 08_회고_보고서 파일로 내보내주고, 
프롬프트를 포함해서 Prompting 폴더의 08_회고_보고서-Prompting.md 파일로 내보내줘
```

의도:

- 이번 세션에서 수행한 회고 문서 작성 작업을 별도 보고서로 남김
- 실제 사용자 프롬프트와 작업 진행 흐름을 Prompting 문서로 기록
- 기존 01~07 산출물 흐름에 맞춰 08번 문서 생성

## 3. 코드 및 문서 확인 단계 프롬프트 구조

회고 문서 작성 전 다음 관점으로 코드베이스와 기존 보고서를 확인했다.

```text
Inspect the current codebase and existing reports for a refactoring retrospective.

Read:
- src/main/java/com/example/demo/FeedbackController.java
- src/main/java/com/example/demo/FeedbackService.java
- src/main/java/com/example/demo/TextAnalyzer.java
- src/main/java/com/example/demo/Filters.java
- src/main/java/com/example/demo/SentimentKeywordFileDb.java
- src/main/java/com/example/demo/FeedbackTrendFileDb.java
- src/main/java/com/example/demo/Constants.java
- src/main/resources/templates/index.html
- src/main/resources/application.properties
- src/test/java/com/example/demo/DemoApplicationTests.java
- report/02_RED-테스트 구조 개선_보고서.md
- report/03_GREEN-버그수정 3건 점검_보고서.md
- report/04_REFACTORING-네이밍& 매직넘버개선_보고서.md
- report/05_REFACTORING-긴함수& 중복제거_보고서.md
- report/06_REFACTORING-SRP 리팩토링_보고서.md
- report/07_FEATURE-추가기능구현_보고서.md

Check:
1. Which refactoring changes were performed.
2. Which files represent the current After state.
3. Which reports describe the old Before state.
4. Whether the current Git diff still contains pre-refactoring code.
5. Which Before/After examples best explain the refactoring.
```

확인 결과:

- 현재 Git diff에는 리팩토링 이전 코드가 직접 남아 있지 않았다.
- 기존 보고서에는 테스트 구조 개선, 버그 수정, 네이밍 개선, 긴 함수 분해, SRP 리팩토링, 기능 추가 흐름이 기록되어 있었다.
- 현재 구현은 `FeedbackController`, `FeedbackService`, `TextAnalyzer`, `Filters`, `SentimentKeywordFileDb`, `FeedbackTrendFileDb`를 중심으로 After 구조를 확인할 수 있었다.
- Before 코드는 기존 보고서의 설명과 코드 스멜을 기준으로 대표 예시를 구성하는 것이 적절했다.

## 4. 비교 보고서 작성 계획 프롬프트 구조

문서 작성 전 다음 기준으로 구성했다.

```text
Create docs/retrospective.md as a Markdown retrospective.

Include:
- Overview of the refactoring purpose.
- Before/After code examples.
- Problems in the previous structure.
- Improvements in the current structure.
- Test and verification summary.
- Remaining technical debt and next steps.

Cover these topics:
1. Test structure improvement.
2. Naming and magic value cleanup.
3. Long method and duplication removal.
4. Controller responsibility separation.
5. CSV upload handling improvement.
6. Sentiment keyword File DB.
7. Trend visualization.
8. Current architecture summary.

Constraints:
- Use Markdown.
- Keep examples concise but concrete.
- Make clear when Before code is a representative reconstruction from reports.
- Save the result to docs/retrospective.md.
```

판단:

- 단순 변경 목록보다 리팩토링 의도와 효과가 드러나도록 구성한다.
- 각 항목은 Before 코드, 문제점, After 코드, 개선 효과 순서로 작성한다.
- 현재 실제 코드와 맞는 After 예시를 우선 사용한다.
- Before 예시는 실제 이전 소스를 복원한 것이 아니라 대표 구조임을 명시한다.

## 5. 회고 문서 구현 프롬프트 구조

`docs/retrospective.md` 작성 시 적용한 구조는 다음과 같다.

```text
Write the retrospective with these sections:

1. 개요
2. 테스트 구조 개선
3. 네이밍과 매직 값 개선
4. 긴 함수와 중복 로직 제거
5. Controller 책임 분리
6. CSV 업로드 처리 개선
7. 감정 키워드 File DB화
8. Trend 시각화 추가
9. 현재 구조 요약
10. 검증 결과
11. 남은 개선 과제
12. 결론

For each Before/After section:
- Show a representative Before Java or HTML snippet.
- Explain the problem briefly.
- Show the current After snippet.
- Summarize the improvement.
```

핵심 작성 내용:

- 기존 테스트가 `contextLoads()` 중심이었다는 점과 현재 단위 테스트 구조 비교
- `sent`, `kw`, `res`, `txt` 같은 축약명에서 도메인 중심 이름으로 변경된 점 정리
- 분석 로직의 정규화, 키워드 매칭, 카운트 증가 책임 분리 설명
- Controller에서 Service로 비즈니스 흐름이 이동한 내용 정리
- CSV 업로드가 임시 파일 방식에서 stream 기반 처리로 바뀐 점 설명
- 감정 키워드가 코드 중복 정의에서 `SentimentKeywordFileDb`로 통합된 점 정리
- `test_feedback_trend.csv` 기반 Trend 시각화 추가 내용 정리

## 6. 검증 프롬프트 구조

문서 생성 후 다음 확인을 수행했다.

```text
Verify the generated retrospective document.

Check:
1. docs/retrospective.md exists.
2. The first part of the file is readable.
3. Markdown linter diagnostics do not report errors.
4. Git status shows the file as newly created.
```

확인 결과:

- `docs/retrospective.md` 파일 생성 확인
- 파일 앞부분 정상 읽기 확인
- IDE linter 오류 없음
- `git status --short docs/retrospective.md`에서 신규 파일로 확인

## 7. 08번 보고서 작성 프롬프트 구조

사용자의 후속 요청에 따라 이번 세션 자체를 보고서로 정리했다.

```text
Create report/08_회고_보고서.md.

Include:
- Work overview.
- Referenced files.
- Initial state.
- Work performed.
- Generated output.
- Verification result.
- Notes discovered during the work.
- Recommended next steps.
- Conclusion.
```

작성 결과:

- `report/08_회고_보고서.md` 생성
- `docs/retrospective.md` 작성 과정과 결과를 보고서 형식으로 정리
- 문서 작성 작업이므로 `mvn test`를 실행하지 않았다는 점 명시

## 8. 08번 Prompting 문서 작성 프롬프트 구조

마지막으로 사용자 프롬프트와 작업 흐름을 이 문서에 기록했다.

```text
Create Prompting/08_회고_보고서-Prompting.md.

Include:
- Original user request for docs/retrospective.md.
- Follow-up user request for report and prompting files.
- Code/document inspection prompt structure.
- Retrospective writing plan.
- Verification prompt structure.
- Final output summary.
```

작성 기준:

- 기존 `Prompting/07_FEATURE-추가기능구현_보고서-Prompting.md`의 형식을 따른다.
- 사용자 요청 문장은 원문 그대로 코드 블록에 기록한다.
- 내부 작업 흐름은 실제 수행한 확인, 판단, 작성, 검증 순서로 정리한다.

## 9. 최종 산출물

이번 세션에서 생성한 파일은 다음과 같다.

- `docs/retrospective.md`
- `report/08_회고_보고서.md`
- `Prompting/08_회고_보고서-Prompting.md`

## 10. 요약

이번 세션의 핵심 프롬프트는 리팩토링 전후 비교 회고 문서를 만들고, 그 작업 과정을 다시 보고서와 Prompting 문서로 남기는 것이었다.

최종적으로 `docs/retrospective.md`에는 리팩토링 전후 코드 비교가 포함되었고, `report/08_회고_보고서.md`와 `Prompting/08_회고_보고서-Prompting.md`에는 이번 세션의 작업 내용과 프롬프트 흐름이 정리되었다.

