package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TextAnalyzer {

    private static Map<String, Integer> globalSent = null;
    private static Map<String, Integer> globalKw = null;

    public Map<String, Integer> sent(List<Feedback> feedbacks) {
        Map<String, Integer> res = new HashMap<>();
        res.put("긍정", 0);
        res.put("중립", 0);
        res.put("부정", 0);

        for (Feedback f : feedbacks) {
            String txt = f.getText().toLowerCase();
            String s = "중립";
            if (Constants.SENTIMENT_KEYWORDS.get("긍정").stream().anyMatch(k -> txt.contains(k))) {
                s = "긍정";
            } else if (Constants.SENTIMENT_KEYWORDS.get("부정").stream().anyMatch(k -> txt.contains(k))) {
                s = "부정";
            }
            res.put(s, res.get(s) + 1);
        }

        globalSent = res;
        return res;
    }

    public Map<String, Integer> kw(List<Feedback> feedbacks) {
        Map<String, Integer> res2 = new HashMap<>();
        for (String category : Constants.CATEGORY_KEYWORDS.keySet()) {
            res2.put(category, 0);
        }

        for (Feedback f : feedbacks) {
            String txt = f.getText().toLowerCase();
            for (Map.Entry<String, Map<String, Object>> entry : Constants.CATEGORY_KEYWORDS.entrySet()) {
                String cat = entry.getKey();
                @SuppressWarnings("unchecked")
                List<String> kws = (List<String>) entry.getValue().get("main");
                if (kws.stream().anyMatch(kw -> txt.contains(kw))) {
                    res2.put(cat, res2.get(cat) + 1);
                }
            }
        }

        globalKw = res2;
        return res2;
    }
}
