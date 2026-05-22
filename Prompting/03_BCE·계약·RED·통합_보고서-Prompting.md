# 03. BCE·계약·RED·통합 보고서 - Prompting 기록

## 1. 목적

이 문서는 이번 세션에서 BCE 설계, 계약 정의, RED 단계 테스트 제목, 통합 검증 계획을 만들기 위해 사용한 프롬프트와 산출 과정을 기록한다.

이번 세션은 구현이 아니라 설계와 테스트 가능성에 초점을 맞췄다.

## 2. 사용자 프롬프트 기록

### 2.1. BCE 설계 전문가 역할 요청

```text
🔲 BCE 설계 전문가 — Java
Dual-Track(UI/경계 vs Domain) + BCE 관점 설계 전문가로 행동하라.
대상: @c:\dev\feedback_analyzer_A02\ 
현재 코드 상태: 단일 main() 함수, if-else 체인, OCP/SRP 미적용
제약: 구현 코드 작성 금지. 설계·계약·테스트 목록·통합 계획만.

# 1) Entity(Domain) 설계
  1.1 개념 목록과 SRP (ConversionRule, UnitRegistry, Converter)
  1.2 Invariants (예: meter 허브 비율 불변, 중복 단위 등록 금지)
  1.3 유스케이스 (단위 변환, 동적 단위 등록, 설정 파일 로드)
  1.4 Domain API (시그니처 수준, 본문 X) + 실패 조건
  1.5 Domain 단위 테스트 설계 (RED 우선)

# 2) Boundary 설계
  2.1 시나리오: 입력(단위:값) → 검증 → 변환 → 출력
  2.2 외부 계약: Input schema / Output schema / Error schema
  2.3 Boundary 계약 테스트 (Domain Mock 가정)
  2.4 에러 메시지 규칙 (음수·잘못된 형식·없는 단위·중복 단위)

# 3) Data 설계
  3.1 목적 (설정 외부화 — JSON/YAML 로드)
  3.2 인터페이스 계약 (loadRatios/saveRatios 이름만)
  3.3 InMemory vs File(JSON/YAML) 비교 + 추천 1개
  3.4 Data 레이어 테스트

# 4) Integration & Verification
  4.1 의존성 방향 포함 흐름
  4.2 통합 테스트 시나리오 (정상 2+, 실패 3+)
  4.3 회귀 보호 규칙
  4.4 커버리지 목표 (Domain / Boundary / Data 수치)
  4.5 Traceability Matrix

모호어 금지. 모든 규칙은 테스트로 검증 가능하게.
```

### 2.2. JUnit 5 RED 테스트 제목 요청

```text
JUnit 5를 가정하고 "구현 없이" RED 단계 테스트 케이스 제목 25~40개.

분류:
- 텍스트 피드백 입력 (수동/CSV 업로드)
- 키워드 기반 피드백 분류
- 감정 분석 (긍정/부정/중립)
- 피드백 필터링 및 검색
- 분석 결과 시각화
- 결과 CSV 다운로드

각 테스트 옆에 보호하는 Invariant 이름 한 줄. 코드 작성 금지.
```

### 2.3. 보고서 및 Prompting 파일 내보내기 요청

```text
이번세션에서 진행한 내용을 report 폴더의 03_BCE·계약·RED·통합_보고서 파일로 내보내주고, 
프롬프트를 포함해서 Prompting 폴더의 03_BCE·계약·RED·통합_보고서-Prompting.md 파일로 내보내줘
```

## 3. 참고한 프로젝트 맥락

세션 중 확인한 프로젝트 맥락은 다음과 같다.

- 프로젝트는 Spring Boot + Thymeleaf 기반 고객 피드백 분석 시스템이다.
- 주요 기능은 수동 텍스트 입력, CSV 업로드, 키워드 기반 분류, 감정 분석, 필터링, 시각화, CSV 다운로드다.
- 현재 `FeedbackController`가 요청 처리, 상태 접근, CSV 처리, 분석 호출, 다운로드 응답을 함께 담당한다.
- `TextAnalyzer`와 `Filters`가 서로 다른 기준을 가질 수 있어 분석과 필터링 결과가 불일치할 위험이 있다.
- `Session`은 static 전역 상태를 사용하므로 사용자별 데이터 격리와 테스트 격리에 취약하다.
- 기존 테스트는 Spring Context 로드만 확인한다.

## 4. 산출 과정 요약

### 4.1. BCE 설계 산출

첫 번째 산출에서는 요청된 단위 변환 도메인을 기준으로 BCE 설계를 구성했다.

핵심 산출은 다음과 같다.

- `ConversionRule`, `UnitRegistry`, `Converter`의 SRP 정의
- meter 허브 ratio 불변, 중복 단위 등록 금지, 음수 변환 금지 등 invariant 정의
- 단위 변환, 동적 단위 등록, 설정 파일 로드 유스케이스 정의
- Domain API 시그니처 수준 계약과 실패 조건 정의
- Boundary input/output/error schema 정의
- Data 저장소 계약과 YAML 추천
- Integration 흐름, 회귀 보호 규칙, 커버리지 목표, Traceability Matrix 정의

### 4.2. RED 테스트 목록 산출

두 번째 산출에서는 Feedback Analyzer의 실제 기능 분류에 맞춰 JUnit 5 RED 단계 테스트 제목을 만들었다.

테스트는 다음 6개 범주로 나눴다.

- 텍스트 피드백 입력
- 키워드 기반 피드백 분류
- 감정 분석
- 피드백 필터링 및 검색
- 분석 결과 시각화
- 결과 CSV 다운로드

총 38개의 테스트 제목을 제안했고, 각 테스트 옆에 보호하는 invariant 이름을 붙였다.

## 5. 주요 설계 원칙

이번 세션에서 적용한 설계 원칙은 다음과 같다.

- Boundary는 입력 검증과 응답 계약을 담당한다.
- Domain은 분석 기준과 invariant를 담당한다.
- Data는 파일 또는 설정 저장소 접근만 담당한다.
- Control은 흐름 조립만 담당한다.
- 에러 메시지와 에러 코드는 테스트에서 exact assertion으로 보호한다.
- 분석 기준과 필터 기준은 하나의 사전에서 파생되어야 한다.
- CSV 다운로드 형식은 header, encoding, escaping 규칙을 테스트로 고정한다.
- 시각화 모델 생성은 원본 피드백 데이터를 변경하지 않아야 한다.

## 6. 최종 산출 파일

이번 요청으로 생성한 파일은 다음과 같다.

- `report/03_BCE·계약·RED·통합_보고서.md`
- `Prompting/03_BCE·계약·RED·통합_보고서-Prompting.md`
