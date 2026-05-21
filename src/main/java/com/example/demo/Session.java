package com.example.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {
    private static List<Feedback> currentFeedbacks = new ArrayList<>();
    private static Map<String, Object> internalData = new HashMap<>();
    private static Map<String, Object> filterOptions = new HashMap<>();

    public static void initSessionStateUgly() {

        if (currentFeedbacks == null) {
            currentFeedbacks = new ArrayList<>();
        }
        if (internalData == null) {
            internalData = new HashMap<>();
        }
        if (filterOptions == null) {
            filterOptions = new HashMap<>();
        }
    }

    public static List<Feedback> getOldDataFromSession(String key) {
        if ("current_feedbacks".equals(key)) {
            return currentFeedbacks;
        }
        return new ArrayList<>();
    }

    public static void updateInternalData(String key, Object value) {
        internalData.put(key, value);
        if ("current_feedbacks".equals(key)) {
            currentFeedbacks = (List<Feedback>) value;
        }
    }

    public static List<Feedback> getCurrentFeedbacks() {
        return currentFeedbacks;
    }
}
