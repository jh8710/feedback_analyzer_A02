package com.example.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final Map<String, List<String>> SENTIMENT_KEYWORDS = new HashMap<>();
    public static final Map<String, Map<String, Object>> CATEGORY_KEYWORDS = new HashMap<>();

    static {
        // Sentiment keywords
        SENTIMENT_KEYWORDS.put("긍정", Arrays.asList(
                "좋아요", "만족", "감사", "최고", "좋은", "훌륭", "추천", "좋았어요",
                "좋습니다", "최고입니다", "감사합니다", "만족스럽", "좋았습니다",
                "최고에요", "기뻐요", "만족합니다", "굿", "최고다", "와우", "아주 좋아",
                "좋아요", "만족", "감사", "최고", "좋은", "훌륭", "추천", "좋았어요",
                "좋습니다", "최고입니다", "감사합니다", "만족스럽", "좋았습니다"
        ));

        SENTIMENT_KEYWORDS.put("부정", Arrays.asList(
                "나쁘", "불만", "실망", "최악", "별로", "불편", "불만족", "문제",
                "불량", "불량품", "환불", "교환", "불만족스럽", "실망스럽",
                "짜증", "화남", "별로에요", "엉망", "최악이다", "실패", "구려",
                "나쁘", "불만", "실망", "최악", "별로", "불편", "불만족", "문제",
                "불량", "불량품", "환불", "교환", "불만족스럽", "실망스럽"
        ));

        // Category keywords
        Map<String, Object> deliveryKeywords = new HashMap<>();
        deliveryKeywords.put("main", Arrays.asList("배송", "택배", "배달", "물류", "배송지연", "배송시간", "퀵", "소포"));
        Map<String, List<String>> deliverySub = new HashMap<>();
        deliverySub.put("time", Arrays.asList("배송지연", "배송시간", "퀵"));
        deliverySub.put("type", Arrays.asList("택배", "배달", "소포"));
        deliverySub.put("status", Arrays.asList("물류", "배송"));
        deliveryKeywords.put("sub", deliverySub);
        CATEGORY_KEYWORDS.put("배송", deliveryKeywords);

        Map<String, Object> qualityKeywords = new HashMap<>();
        qualityKeywords.put("main", Arrays.asList("품질", "재질", "내구성", "마감", "제품상태", "품질문제", "내용물", "고장"));
        Map<String, List<String>> qualitySub = new HashMap<>();
        qualitySub.put("physical", Arrays.asList("재질", "내구성", "마감"));
        qualitySub.put("state", Arrays.asList("제품상태", "품질문제", "고장"));
        qualitySub.put("content", Arrays.asList("내용물"));
        qualityKeywords.put("sub", qualitySub);
        CATEGORY_KEYWORDS.put("품질", qualityKeywords);

        Map<String, Object> priceKeywords = new HashMap<>();
        priceKeywords.put("main", Arrays.asList("가격", "비용", "할인", "가성비", "가격대", "비싸", "저렴", "금액", "요금"));
        Map<String, List<String>> priceSub = new HashMap<>();
        priceSub.put("amount", Arrays.asList("가격", "비용", "금액", "요금"));
        priceSub.put("discount", Arrays.asList("할인", "가성비", "가격대"));
        priceSub.put("evaluation", Arrays.asList("비싸", "저렴"));
        priceKeywords.put("sub", priceSub);
        CATEGORY_KEYWORDS.put("가격", priceKeywords);

        Map<String, Object> serviceKeywords = new HashMap<>();
        serviceKeywords.put("main", Arrays.asList("서비스", "응대", "상담", "문의", "답변", "고객서비스", "친절", "불친절"));
        Map<String, List<String>> serviceSub = new HashMap<>();
        serviceSub.put("interaction", Arrays.asList("응대", "상담", "문의", "답변"));
        serviceSub.put("quality", Arrays.asList("친절", "불친절"));
        serviceSub.put("type", Arrays.asList("서비스", "고객서비스"));
        serviceKeywords.put("sub", serviceSub);
        CATEGORY_KEYWORDS.put("서비스", serviceKeywords);

        Map<String, Object> usabilityKeywords = new HashMap<>();
        usabilityKeywords.put("main", Arrays.asList("사용", "편리", "불편", "사용법", "설명서", "사용방법", "어렵", "쉽게"));
        Map<String, List<String>> usabilitySub = new HashMap<>();
        usabilitySub.put("ease", Arrays.asList("편리", "불편", "쉽게", "어렵"));
        usabilitySub.put("guide", Arrays.asList("사용법", "설명서", "사용방법"));
        usabilitySub.put("action", Arrays.asList("사용"));
        usabilityKeywords.put("sub", usabilitySub);
        CATEGORY_KEYWORDS.put("사용성", usabilityKeywords);
    }
}
