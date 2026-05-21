package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.demo.Constants.CATEGORY_KEYWORDS;

@Service
public class Filters {

    private static final Map<String, List<String>> S_KEYWORDS = new HashMap<>();

    static {
        S_KEYWORDS.put("긍정", Arrays.asList(
                "좋아요", "만족", "감사", "친절", "좋다", "좋았", "좋은", "우수", "빠르", "정확", "신속", "안전", "괜찮", "인상적", "추천", "기대 이상", "합리", "꼼꼼", "뛰어납니다", "만족스럽", "좋았습니다", "좋습니다", "만족합니다", "굿", "최고", "최고입니다", "감사합니다"
        ));
        S_KEYWORDS.put("부정", Arrays.asList(
                "나쁘", "불만", "실망", "최악", "별로", "불편", "불만족", "문제",
                "불량", "불량품", "환불", "교환", "불만족스럽", "실망스럽", "비싸", "불친절", "늦다"
        ));
        S_KEYWORDS.put("중립", Arrays.asList(
                "괜찮", "보통", "평범", "무난", "그냥", "전반적", "완료", "적당", "나쁘지 않", "특별", "없"
        ));
    }

    public List<Feedback> fil(List<Feedback> dataList, String sFilter, String kFilter) {
        List<Feedback> tmpFiltered = new ArrayList<>();

        if (!"전체".equals(sFilter)) {
            for (Feedback item : dataList) {
                String txt = item.getText().toLowerCase();
                String currentSentiment = "중립";

                if (S_KEYWORDS.get("긍정").stream().anyMatch(key -> txt.contains(key))) {
                    currentSentiment = "긍정";
                } else if (S_KEYWORDS.get("부정").stream().anyMatch(key -> txt.contains(key))) {
                    currentSentiment = "부정";
                } else if (S_KEYWORDS.get("중립").stream().anyMatch(key -> txt.contains(key))) {
                    currentSentiment = "중립";
                }

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
}
