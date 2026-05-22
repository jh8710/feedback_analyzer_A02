package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TextAnalyzer {

    private static Map<String, Integer> latestSentimentCounts = null;
    private static Map<String, Integer> latestCategoryCounts = null;

    public Map<String, Integer> analyzeSentiments(List<Feedback> feedbacks) {
        Map<String, Integer> sentimentCounts = new HashMap<>();
        sentimentCounts.put(Constants.SENTIMENT_POSITIVE, Constants.INITIAL_ANALYSIS_COUNT);
        sentimentCounts.put(Constants.SENTIMENT_NEUTRAL, Constants.INITIAL_ANALYSIS_COUNT);
        sentimentCounts.put(Constants.SENTIMENT_NEGATIVE, Constants.INITIAL_ANALYSIS_COUNT);

        for (Feedback feedback : feedbacks) {
            String normalizedFeedbackText = feedback.getText().toLowerCase();
            String detectedSentiment = Constants.SENTIMENT_NEUTRAL;
            if (Constants.SENTIMENT_KEYWORDS.get(Constants.SENTIMENT_POSITIVE).stream().anyMatch(keyword -> normalizedFeedbackText.contains(keyword))) {
                detectedSentiment = Constants.SENTIMENT_POSITIVE;
            } else if (Constants.SENTIMENT_KEYWORDS.get(Constants.SENTIMENT_NEGATIVE).stream().anyMatch(keyword -> normalizedFeedbackText.contains(keyword))) {
                detectedSentiment = Constants.SENTIMENT_NEGATIVE;
            }
            sentimentCounts.put(detectedSentiment, sentimentCounts.get(detectedSentiment) + 1);
        }

        latestSentimentCounts = sentimentCounts;
        return sentimentCounts;
    }

    public Map<String, Integer> analyzeCategoryKeywords(List<Feedback> feedbacks) {
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (String category : Constants.CATEGORY_KEYWORDS.keySet()) {
            categoryCounts.put(category, Constants.INITIAL_ANALYSIS_COUNT);
        }

        for (Feedback feedback : feedbacks) {
            String normalizedFeedbackText = feedback.getText().toLowerCase();
            for (Map.Entry<String, Map<String, Object>> categoryEntry : Constants.CATEGORY_KEYWORDS.entrySet()) {
                String categoryName = categoryEntry.getKey();
                @SuppressWarnings("unchecked")
                List<String> categoryMainKeywords = (List<String>) categoryEntry.getValue().get(Constants.CATEGORY_MAIN_KEY);
                if (categoryMainKeywords.stream().anyMatch(keyword -> normalizedFeedbackText.contains(keyword))) {
                    categoryCounts.put(categoryName, categoryCounts.get(categoryName) + 1);
                }
            }
        }

        latestCategoryCounts = categoryCounts;
        return categoryCounts;
    }
}
