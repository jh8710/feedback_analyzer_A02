package com.example.demo;

import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SentimentKeywordFileDb {
    private static final String DEFAULT_FILE_PATH = "data/sentiment_keywords.csv";
    private static final List<String> SENTIMENT_PRIORITY = List.of(
            Constants.SENTIMENT_NEUTRAL,
            Constants.SENTIMENT_NEGATIVE,
            Constants.SENTIMENT_POSITIVE
    );

    private String filePath = DEFAULT_FILE_PATH;

    public SentimentKeywordFileDb() {
    }

    public SentimentKeywordFileDb(String filePath) {
        this.filePath = filePath;
    }

    @Value("${feedback.sentiment-keywords.path:data/sentiment_keywords.csv}")
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<String> getSentimentLabels() {
        return new ArrayList<>(loadKeywords().keySet());
    }

    public List<String> getKeywords(String sentiment) {
        return loadKeywords().getOrDefault(sentiment, Collections.emptyList());
    }

    public String detectSentiment(String text) {
        String normalizedText = text.toLowerCase();
        Map<String, List<String>> keywords = loadKeywords();

        for (String sentiment : SENTIMENT_PRIORITY) {
            if (containsAnyKeyword(normalizedText, keywords.getOrDefault(sentiment, Collections.emptyList()))) {
                return sentiment;
            }
        }

        return Constants.SENTIMENT_NEUTRAL;
    }

    private Map<String, List<String>> loadKeywords() {
        try {
            Path path = Path.of(filePath);
            if (Files.exists(path)) {
                try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
                    return readKeywords(csvReader);
                }
            }

            InputStream resource = getClass().getClassLoader().getResourceAsStream(filePath);
            if (resource != null) {
                try (CSVReader csvReader = new CSVReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                    return readKeywords(csvReader);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("감정 키워드 File DB를 읽을 수 없습니다: " + filePath, e);
        }

        return defaultKeywords();
    }

    private Map<String, List<String>> readKeywords(CSVReader csvReader) throws Exception {
        Map<String, List<String>> keywords = initializeKeywords();

        csvReader.readNext();
        String[] line;
        while ((line = csvReader.readNext()) != null) {
            if (line.length < 2) {
                continue;
            }

            String sentiment = removeBom(line[0]).trim();
            String keyword = line[1].trim().toLowerCase();
            if (!sentiment.isEmpty() && !keyword.isEmpty()) {
                keywords.computeIfAbsent(sentiment, ignored -> new ArrayList<>()).add(keyword);
            }
        }

        return keywords;
    }

    private Map<String, List<String>> initializeKeywords() {
        Map<String, List<String>> keywords = new LinkedHashMap<>();
        keywords.put(Constants.SENTIMENT_POSITIVE, new ArrayList<>());
        keywords.put(Constants.SENTIMENT_NEUTRAL, new ArrayList<>());
        keywords.put(Constants.SENTIMENT_NEGATIVE, new ArrayList<>());
        return keywords;
    }

    private Map<String, List<String>> defaultKeywords() {
        Map<String, List<String>> keywords = initializeKeywords();
        keywords.put(Constants.SENTIMENT_POSITIVE, new ArrayList<>(Constants.SENTIMENT_KEYWORDS.get(Constants.SENTIMENT_POSITIVE)));
        keywords.put(Constants.SENTIMENT_NEGATIVE, new ArrayList<>(Constants.SENTIMENT_KEYWORDS.get(Constants.SENTIMENT_NEGATIVE)));
        keywords.put(Constants.SENTIMENT_NEUTRAL, new ArrayList<>(List.of(
                "괜찮", "보통", "평범", "무난", "그냥", "전반적", "완료", "적당", "나쁘지 않", "특별", "없"
        )));
        return keywords;
    }

    private String removeBom(String value) {
        return value.replace("\uFEFF", "");
    }

    private boolean containsAnyKeyword(String normalizedText, List<String> keywords) {
        return keywords.stream().anyMatch(normalizedText::contains);
    }
}
