package com.example.demo;

import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    @Autowired
    private TextAnalyzer textAnalyzer;

    @Autowired
    private Filters filters;

    @Autowired
    private Logger logger;

    @Autowired
    private FeedbackTrendFileDb feedbackTrendFileDb;

    private List<Feedback> filteredFeedbacks = new ArrayList<>();

    public List<Feedback> getFeedbacksForIndex() {
        Session.initSessionStateUgly();
        return Session.getOldDataFromSession("current_feedbacks");
    }

    public AnalysisResult addAndAnalyze(String text) {
        List<Feedback> feedbacks = Session.getCurrentFeedbacks();

        if (text != null && !text.trim().isEmpty()) {
            feedbacks.add(new Feedback(text.trim()));
        }

        for (Feedback feedback : feedbacks) {
            logger.logInfo("%s", feedback.getText());
        }

        Session.updateInternalData("current_feedbacks", feedbacks);
        logger.logInfo("현재 %d개의 피드백이 입력되었습니다.", feedbacks.size());

        if (feedbacks.isEmpty()) {
            return AnalysisResult.empty(feedbacks);
        }

        Map<String, Integer> sentimentResults = textAnalyzer.analyzeSentiments(feedbacks);
        Map<String, Integer> keywordResults = textAnalyzer.analyzeCategoryKeywords(feedbacks);

        logger.logInfo("감성 분석 완료");
        logger.logInfo("키워드 분석 완료");

        return new AnalysisResult(feedbacks, sentimentResults, keywordResults);
    }

    public List<Feedback> uploadFeedbacks(MultipartFile file) {
        if (file.isEmpty()) {
            return Session.getCurrentFeedbacks();
        }

        List<Feedback> feedbacks = Session.getCurrentFeedbacks();

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] line;
            csvReader.readNext();
            while ((line = csvReader.readNext()) != null) {
                feedbacks.add(new Feedback(line[0]));
            }
        } catch (Exception e) {
            logger.logError("csv 파일 처리 오류: %s", e.getMessage());
            throw new RuntimeException(e);
        }

        Session.updateInternalData("current_feedbacks", feedbacks);
        logger.logInfo("파일이 성공적으로 업로드되었습니다.");

        return feedbacks;
    }

    public FilterResult filterFeedbacks(String sentiment, String keyword) {
        List<Feedback> feedbacks = Session.getCurrentFeedbacks();

        if (feedbacks.isEmpty()) {
            logger.logWarning("분석할 피드백이 없습니다.");
            return FilterResult.warning("분석할 피드백이 없습니다.");
        }

        List<Feedback> filtered = filters.fil(feedbacks, sentiment, keyword);

        if (filtered.isEmpty()) {
            logger.logWarning("필터링 결과가 없습니다.");
            return FilterResult.warning("필터링 결과가 없습니다.");
        }

        filteredFeedbacks = filtered;
        Map<String, Integer> sentimentResults = textAnalyzer.analyzeSentiments(filtered);
        Map<String, Integer> keywordResults = textAnalyzer.analyzeCategoryKeywords(filtered);

        logger.logInfo("필터링 결과: %d개의 피드백", filtered.size());

        return new FilterResult(filtered, sentimentResults, keywordResults, null);
    }

    public List<Feedback> getFilteredFeedbacks() {
        return filteredFeedbacks;
    }

    public List<FeedbackTrendFileDb.TrendPoint> getTrendPoints() {
        return feedbackTrendFileDb.getTrendPoints();
    }

    public void updateLogLevel(String logLevel) {
        logger.setLogLevel(logLevel);
    }

    public String getLogLevel() {
        return logger.getLogLevel();
    }

    public static class AnalysisResult {
        private final List<Feedback> feedbacks;
        private final Map<String, Integer> sentimentResults;
        private final Map<String, Integer> keywordResults;

        private AnalysisResult(List<Feedback> feedbacks, Map<String, Integer> sentimentResults, Map<String, Integer> keywordResults) {
            this.feedbacks = feedbacks;
            this.sentimentResults = sentimentResults;
            this.keywordResults = keywordResults;
        }

        private static AnalysisResult empty(List<Feedback> feedbacks) {
            return new AnalysisResult(feedbacks, null, null);
        }

        public List<Feedback> getFeedbacks() {
            return feedbacks;
        }

        public Map<String, Integer> getSentimentResults() {
            return sentimentResults;
        }

        public Map<String, Integer> getKeywordResults() {
            return keywordResults;
        }

        public boolean hasResults() {
            return sentimentResults != null && keywordResults != null;
        }
    }

    public static class FilterResult {
        private final List<Feedback> filteredFeedbacks;
        private final Map<String, Integer> sentimentResults;
        private final Map<String, Integer> keywordResults;
        private final String warning;

        private FilterResult(List<Feedback> filteredFeedbacks, Map<String, Integer> sentimentResults, Map<String, Integer> keywordResults, String warning) {
            this.filteredFeedbacks = filteredFeedbacks;
            this.sentimentResults = sentimentResults;
            this.keywordResults = keywordResults;
            this.warning = warning;
        }

        private static FilterResult warning(String warning) {
            return new FilterResult(List.of(), null, null, warning);
        }

        public List<Feedback> getFilteredFeedbacks() {
            return filteredFeedbacks;
        }

        public Map<String, Integer> getSentimentResults() {
            return sentimentResults;
        }

        public Map<String, Integer> getKeywordResults() {
            return keywordResults;
        }

        public String getWarning() {
            return warning;
        }

        public boolean hasResults() {
            return warning == null;
        }
    }
}
