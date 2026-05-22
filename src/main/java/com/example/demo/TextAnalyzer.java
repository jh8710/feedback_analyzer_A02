package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TextAnalyzer {

    private static Map<String, Integer> latestSentimentCounts = null;
    private static Map<String, Integer> latestCategoryCounts = null;

    private final SentimentKeywordFileDb sentimentKeywordFileDb;

    public TextAnalyzer() {
        this(new SentimentKeywordFileDb());
    }

    @Autowired
    public TextAnalyzer(SentimentKeywordFileDb sentimentKeywordFileDb) {
        this.sentimentKeywordFileDb = sentimentKeywordFileDb;
    }

    public Map<String, Integer> analyzeSentiments(List<Feedback> feedbacks) {
        Map<String, Integer> sentimentCounts = initializeCounts(sentimentKeywordFileDb.getSentimentLabels());

        for (Feedback feedback : feedbacks) {
            incrementCount(sentimentCounts, detectSentiment(normalize(feedback)));
        }

        latestSentimentCounts = sentimentCounts;
        return sentimentCounts;
    }

    public Map<String, Integer> analyzeCategoryKeywords(List<Feedback> feedbacks) {
        Map<String, Integer> categoryCounts = initializeCounts(Constants.CATEGORY_KEYWORDS.keySet());

        for (Feedback feedback : feedbacks) {
            countMatchedCategories(categoryCounts, normalize(feedback));
        }

        latestCategoryCounts = categoryCounts;
        return categoryCounts;
    }

    private Map<String, Integer> initializeCounts(Collection<String> labels) {
        Map<String, Integer> counts = new HashMap<>();
        for (String label : labels) {
            counts.put(label, Constants.INITIAL_ANALYSIS_COUNT);
        }
        return counts;
    }

    private String normalize(Feedback feedback) {
        return feedback.getText().toLowerCase();
    }

    private String detectSentiment(String normalizedFeedbackText) {
        return sentimentKeywordFileDb.detectSentiment(normalizedFeedbackText);
    }

    private void countMatchedCategories(Map<String, Integer> categoryCounts, String normalizedFeedbackText) {
        for (Map.Entry<String, Map<String, Object>> categoryEntry : Constants.CATEGORY_KEYWORDS.entrySet()) {
            if (matchesCategory(normalizedFeedbackText, categoryEntry.getValue())) {
                incrementCount(categoryCounts, categoryEntry.getKey());
            }
        }
    }

    private boolean matchesCategory(String normalizedFeedbackText, Map<String, Object> categoryKeywords) {
        return containsAnyKeyword(normalizedFeedbackText, getMainKeywords(categoryKeywords));
    }

    @SuppressWarnings("unchecked")
    private List<String> getMainKeywords(Map<String, Object> categoryKeywords) {
        return (List<String>) categoryKeywords.get(Constants.CATEGORY_MAIN_KEY);
    }

    private boolean containsAnyKeyword(String normalizedFeedbackText, List<String> keywords) {
        return keywords.stream().anyMatch(normalizedFeedbackText::contains);
    }

    private void incrementCount(Map<String, Integer> counts, String label) {
        counts.put(label, counts.get(label) + 1);
    }
}
