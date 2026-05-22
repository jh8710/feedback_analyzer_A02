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
import java.util.List;

@Service
public class FeedbackTrendFileDb {
    private static final String DEFAULT_FILE_PATH = "data/test_feedback_trend.csv";

    private String filePath = DEFAULT_FILE_PATH;

    public FeedbackTrendFileDb() {
    }

    public FeedbackTrendFileDb(String filePath) {
        this.filePath = filePath;
    }

    @Value("${feedback.trend.path:data/test_feedback_trend.csv}")
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<TrendPoint> getTrendPoints() {
        try {
            Path path = Path.of(filePath);
            if (Files.exists(path)) {
                try (CSVReader csvReader = new CSVReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
                    return readTrendPoints(csvReader);
                }
            }

            InputStream resource = getClass().getClassLoader().getResourceAsStream(filePath);
            if (resource != null) {
                try (CSVReader csvReader = new CSVReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                    return readTrendPoints(csvReader);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Trend File DB를 읽을 수 없습니다: " + filePath, e);
        }

        return List.of();
    }

    private List<TrendPoint> readTrendPoints(CSVReader csvReader) throws Exception {
        List<TrendPoint> trendPoints = new ArrayList<>();

        csvReader.readNext();
        String[] line;
        while ((line = csvReader.readNext()) != null) {
            if (line.length < 4) {
                continue;
            }

            trendPoints.add(new TrendPoint(
                    removeBom(line[0]).trim(),
                    parseCount(line[1]),
                    parseCount(line[2]),
                    parseCount(line[3])
            ));
        }

        return trendPoints;
    }

    private int parseCount(String value) {
        return Integer.parseInt(value.trim());
    }

    private String removeBom(String value) {
        return value.replace("\uFEFF", "");
    }

    public static class TrendPoint {
        private final String date;
        private final int positive;
        private final int neutral;
        private final int negative;

        public TrendPoint(String date, int positive, int neutral, int negative) {
            this.date = date;
            this.positive = positive;
            this.neutral = neutral;
            this.negative = negative;
        }

        public String getDate() {
            return date;
        }

        public int getPositive() {
            return positive;
        }

        public int getNeutral() {
            return neutral;
        }

        public int getNegative() {
            return negative;
        }

        public int getTotal() {
            return positive + neutral + negative;
        }
    }
}
