# 01. 프로젝트 개요 & 준비 보고서 - Prompting 기록

## 1. 목적

이 문서는 이번 세션에서 `feedback_analyzer_A02` 프로젝트의 개요와 준비 내용을 정리하기 위해 사용한 프롬프트와 산출 과정을 기록한다.

## 2. 사용자 프롬프트 기록

### 2.1. Cursor 규칙 작성 요청

```text
[P] 당신은 레거시 코드 QA/리팩토링을 돕는 시니어 Java 엔지니어입니다.
[C] @ 프로젝트에서 Cursor AI가 항상 따라야 하는 규칙을
    프로젝트 루트의 .cursorrules로 작성하려고 합니다.
[T] 아래 요구를 만족하는 .cursorrules 내용을 작성해줘.
    - 기술 스택: Java 21 + Maven + JUnit 5 (+ JaCoCo) + Spring boot + thymeleaf
    - 절대 규칙: 필수 컬럼 text, 텍스트 컬럼에 피드백 내용 포함
    - 테스트 규칙: Given-When-Then, @SpringBootTest, @Test
    - 리팩토링 규칙: 테스트 Green 상태에서만 진행, 매직 넘버 상수화 권장
[F] .cursorrules 파일에 그대로 붙여넣을 수 있는 텍스트
```

### 2.2. `.cursorrules` 파일 생성 요청

```text
.cursorrule 파일로 내보내줘
```

실제 생성 파일명은 Cursor 규칙 파일명 관례와 이전 요청을 기준으로 `.cursorrules`로 작성했다.

### 2.3. 프로젝트 구조 및 미션 분석 요청

```text
@c:\dev\feedback_analyzer_A02\ 전체구조·미션 안내 분석 해줘
```

### 2.4. 보고서 및 Prompting 파일 내보내기 요청

```text
이번세션에서 진행한 내용을 report 폴더의 01_프로젝트개요& 준비_보고서 파일로 내보내주고, 
프롬프트를 포함해서 Prompting 폴더의 01_프로젝트개요& 준비_보고서-Prompting.md 파일로 내보내줘
```

## 3. 수행한 작업 요약

### 3.1. `.cursorrules` 작성

프로젝트 루트에 `.cursorrules` 파일을 생성하고 다음 규칙을 반영했다.

- Java 21, Maven, Spring Boot, Thymeleaf, JUnit 5, JaCoCo 기준 명시
- CSV/피드백 데이터의 필수 컬럼 `text` 절대 규칙 명시
- `@SpringBootTest`, `@Test`, Given-When-Then 테스트 작성 규칙 명시
- 테스트 Green 상태에서만 리팩토링 진행
- 매직 넘버와 반복 문자열 상수화 권장
- `target/` 같은 빌드 산출물 수정 금지

### 3.2. 프로젝트 문서 분석

분석 대상 문서는 다음과 같다.

- `README.md`
- `project_purpose.md`
- `pom.xml`

확인한 핵심 내용은 다음과 같다.

- 프로젝트는 고객 피드백 분석 시스템이다.
- Spring Boot + Thymeleaf 기반 웹 애플리케이션이다.
- 텍스트 입력과 CSV 업로드를 통해 피드백을 수집한다.
- 감정 분석, 키워드 분류, 필터링, 결과 다운로드 기능을 제공한다.
- 현재 코드는 리팩토링 학습용으로 의도적인 코드 스멜을 포함한다.

### 3.3. 코드 구조 분석

분석한 주요 파일은 다음과 같다.

- `src/main/java/com/example/demo/FeedbackController.java`
- `src/main/java/com/example/demo/TextAnalyzer.java`
- `src/main/java/com/example/demo/Filters.java`
- `src/main/java/com/example/demo/Constants.java`
- `src/main/java/com/example/demo/Session.java`
- `src/main/java/com/example/demo/Feedback.java`
- `src/main/java/com/example/demo/FileHandler.java`
- `src/main/java/com/example/demo/UIComponents.java`
- `src/main/java/com/example/demo/Logger.java`
- `src/main/resources/templates/index.html`
- `src/test/java/com/example/demo/DemoApplicationTests.java`

확인한 주요 판단은 다음과 같다.

- `FeedbackController`에 요청 처리, CSV 처리, 분석, 필터링, 다운로드 책임이 집중되어 있다.
- `Session`은 static 전역 상태를 사용한다.
- `TextAnalyzer`는 분석 결과를 static 필드에 저장한다.
- `Filters`는 `Constants`와 별도의 감정 키워드를 가지고 있어 중복과 불일치 위험이 있다.
- CSV 업로드는 `text` 헤더 검증 없이 첫 번째 컬럼을 사용한다.
- 테스트는 `contextLoads()` 하나뿐이라 리팩토링 보호막이 부족하다.

## 4. 분석 결과 요약

프로젝트는 기능 구현보다 리팩토링 학습에 초점을 둔 구조다. 현재 상태에서는 테스트를 먼저 보강하고, 이후 컨트롤러 책임 분리와 전역 상태 제거를 진행하는 순서가 적절하다.

우선 개선 후보는 다음과 같다.

1. `TextAnalyzer`, `Filters`, CSV 처리 테스트 추가
2. CSV 필수 컬럼 `text` 검증
3. `FeedbackController`의 비즈니스 로직 서비스 분리
4. 중복 감정 키워드 통합
5. `Session` static 상태 제거
6. Java 21 및 JaCoCo 설정 정합성 확인

## 5. 최종 산출 파일

이번 요청으로 생성한 파일은 다음과 같다.

- `report/01_프로젝트개요& 준비_보고서.md`
- `Prompting/01_프로젝트개요& 준비_보고서-Prompting.md`

앞선 요청으로 생성한 파일은 다음과 같다.

- `.cursorrules`
