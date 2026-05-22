package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.demo.Constants.CATEGORY_KEYWORDS;

@Service
public class Filters {

    private final SentimentKeywordFileDb sentimentKeywordFileDb;

    public Filters() {
        this(new SentimentKeywordFileDb());
    }

    @Autowired
    public Filters(SentimentKeywordFileDb sentimentKeywordFileDb) {
        this.sentimentKeywordFileDb = sentimentKeywordFileDb;
    }

    public List<Feedback> fil(List<Feedback> dataList, String sFilter, String kFilter) {
        List<Feedback> tmpFiltered = new ArrayList<>();

        if (!"전체".equals(sFilter)) {
            for (Feedback item : dataList) {
                String currentSentiment = getSentiment(item.getText());

                if (currentSentiment.equals(sFilter)) {
                    tmpFiltered.add(item);
                }
            }
        } else {
            tmpFiltered = new ArrayList<>(dataList);
        }

        List<Feedback> finalFiltered = new ArrayList<>();
        if (!"전체".equals(kFilter)) {
            for (Feedback item : tmpFiltered) {
                String txt = item.getText().toLowerCase();

                @SuppressWarnings("unchecked")
                Map<String, List<String>> tmpSub = (Map<String, List<String>>)CATEGORY_KEYWORDS.get(kFilter).get("sub");

                for (String key : tmpSub.keySet()) {
                    if(tmpSub.get(key).stream().anyMatch(keyword -> txt.contains(keyword))) {
                        finalFiltered.add(item);
                    }
                }
            }
        } else {
            finalFiltered = new ArrayList<>(tmpFiltered);
        }

        for(Feedback i : finalFiltered) {
            System.out.println(i.getText());
        }

        return finalFiltered;
    }

    private String getSentiment(String text) {
        return sentimentKeywordFileDb.detectSentiment(text);
    }
}
