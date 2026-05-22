package com.example.demo;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void textAnalyzerCountsSentimentsByKeyword() {
		TextAnalyzer analyzer = new TextAnalyzer();
		List<Feedback> feedbacks = List.of(
				new Feedback("배송이 빠르고 제품이 좋아요"),
				new Feedback("품질문제로 환불하고 싶습니다"),
				new Feedback("포장은 보통입니다")
		);

		Map<String, Integer> result = analyzer.sent(feedbacks);

		assertEquals(1, result.get("긍정"));
		assertEquals(1, result.get("부정"));
		assertEquals(1, result.get("중립"));
	}

	@Test
	void textAnalyzerCountsCategoryMatches() {
		TextAnalyzer analyzer = new TextAnalyzer();
		List<Feedback> feedbacks = List.of(
				new Feedback("배송시간이 정확하고 택배가 빨랐습니다"),
				new Feedback("제품 품질과 마감이 좋습니다"),
				new Feedback("가격은 저렴하지만 사용법이 어렵습니다"),
				new Feedback("상담 응대가 친절했습니다"),
				new Feedback("관련 키워드가 없는 의견입니다")
		);

		Map<String, Integer> result = analyzer.kw(feedbacks);

		assertEquals(1, result.get("배송"));
		assertEquals(1, result.get("품질"));
		assertEquals(1, result.get("가격"));
		assertEquals(1, result.get("서비스"));
		assertEquals(1, result.get("사용성"));
	}

	@Test
	void filtersReturnsAllWhenBothFiltersAreAll() {
		Filters filters = new Filters();
		List<Feedback> feedbacks = List.of(
				new Feedback("배송이 좋아요"),
				new Feedback("가격이 비싸요")
		);

		List<Feedback> result = filters.fil(feedbacks, "전체", "전체");

		assertEquals(feedbacks, result);
	}

	@Test
	void filtersBySentimentOnly() {
		Filters filters = new Filters();
		List<Feedback> feedbacks = List.of(
				new Feedback("서비스가 좋아요"),
				new Feedback("배송이 늦고 불만입니다"),
				new Feedback("그냥 보통입니다")
		);

		List<Feedback> result = filters.fil(feedbacks, "부정", "전체");

		assertEquals(1, result.size());
		assertEquals("배송이 늦고 불만입니다", result.get(0).getText());
	}

	@Test
	void filtersByCategoryOnly() {
		Filters filters = new Filters();
		List<Feedback> feedbacks = List.of(
				new Feedback("배송시간이 빨라요"),
				new Feedback("마감과 내구성이 좋습니다"),
				new Feedback("상관없는 의견입니다")
		);

		List<Feedback> result = filters.fil(feedbacks, "전체", "품질");

		assertEquals(1, result.size());
		assertEquals("마감과 내구성이 좋습니다", result.get(0).getText());
	}

	@Test
	void filtersBySentimentAndCategoryTogether() {
		Filters filters = new Filters();
		List<Feedback> feedbacks = List.of(
				new Feedback("택배가 늦고 불만입니다"),
				new Feedback("택배가 좋아요"),
				new Feedback("가격이 비싸서 불만입니다")
		);

		List<Feedback> result = filters.fil(feedbacks, "부정", "배송");

		assertEquals(1, result.size());
		assertEquals("택배가 늦고 불만입니다", result.get(0).getText());
	}

	@Test
	void filtersNeutralKeywordAsNeutralWhenItOverlapsPositiveKeyword() {
		Filters filters = new Filters();
		List<Feedback> feedbacks = List.of(
				new Feedback("서비스가 괜찮습니다"),
				new Feedback("서비스가 좋아요")
		);

		List<Feedback> result = filters.fil(feedbacks, "중립", "전체");

		assertEquals(1, result.size());
		assertEquals("서비스가 괜찮습니다", result.get(0).getText());
	}

	@Test
	void loggerSuppressesWarningsWhenLogLevelIsError() {
		Logger.setLogLevel("error");

		try {
			String output = captureStandardOutput(() -> Logger.logWarning("숨겨질 경고"));

			assertEquals("", output);
		} finally {
			Logger.setLogLevel("warning");
		}
	}

	@Test
	void loggerKeepsErrorsEnabledWhenLogLevelIsError() {
		Logger.setLogLevel("error");

		try {
			String output = captureStandardError(() -> Logger.logError("보여야 할 오류"));

			assertTrue(output.contains("ERROR: 보여야 할 오류"));
		} finally {
			Logger.setLogLevel("warning");
		}
	}

	@Test
	void fileHandlerSaveResultPrintsDataSizeAndFeedbackText() {
		FileHandler fileHandler = new FileHandler();
		List<Feedback> feedbacks = List.of(
				new Feedback("첫 번째 피드백"),
				new Feedback("두 번째 피드백")
		);

		String output = captureStandardOutput(() -> fileHandler.saveResult(feedbacks));

		assertTrue(output.contains("saveResult2"));
		assertTrue(output.contains("첫 번째 피드백"));
		assertTrue(output.contains("두 번째 피드백"));
	}

	@Test
	void fileHandlerSaveDelegatesToSaveResult() {
		FileHandler fileHandler = new FileHandler();
		List<Feedback> feedbacks = List.of(new Feedback("저장할 피드백"));

		String output = captureStandardOutput(() -> assertDoesNotThrow(() -> fileHandler.save(feedbacks)));

		assertTrue(output.contains("saveResult1"));
		assertTrue(output.contains("저장할 피드백"));
	}

	private String captureStandardOutput(Runnable runnable) {
		PrintStream originalOut = System.out;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
		try {
			runnable.run();
		} finally {
			System.setOut(originalOut);
		}
		return outputStream.toString(StandardCharsets.UTF_8);
	}

	private String captureStandardError(Runnable runnable) {
		PrintStream originalErr = System.err;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		System.setErr(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
		try {
			runnable.run();
		} finally {
			System.setErr(originalErr);
		}
		return outputStream.toString(StandardCharsets.UTF_8);
	}
}
