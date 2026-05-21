# Feedback Analyzer
![feedback_analyzer](./feedback_analyzer.png)

고객 피드백 분석 시스템은 자연어 기반 고객 피드백 데이터를 수집, 분류, 시각화하는 기능을 제공하는 Spring boot + thymeleaf 기반 웹 애플리케이션입니다.

## 주요 기능

- 텍스트 피드백 입력 (수동/CSV 업로드)
- 키워드 기반 피드백 분류
- 감정 분석 (긍정/부정/중립)
- 피드백 필터링 및 검색
- 분석 결과 시각화
- 결과 CSV 다운로드

## 설치 방법
저장소 클론
```bash
git clone [repository-url]
cd feedback_analyzer_java
```


## 실행 방법
```bash
mvn spring-boot:run
```

## 프로젝트 구조

```
feedback_analyzer_java/
├── DemoApplication      # 메인 애플리케이션
├── TextAnalyzer         # 텍스트 분석 로직
├── filters              # 필터링
├── UIComponents         # UI 컴포넌트
├── session              # 상태 관리
├── Logger               # 로깅
├── Constant             # 상수 정의
├── test_feedbacks.csv   # sample data
└── README.md            # 프로젝트 설명 
```

## 사용 방법

1. 웹 브라우저에서 `http://localhost:8080` 접속
2. 피드백 텍스트 입력 또는 CSV 파일 업로드
3. 감정/키워드 필터로 결과 필터링
4. 필요시 결과 다운로드

## CSV 파일 형식

입력 CSV 파일은 다음과 같은 형식이어야 합니다:
- 필수 컬럼: `text`
- 텍스트 컬럼에 피드백 내용 포함
