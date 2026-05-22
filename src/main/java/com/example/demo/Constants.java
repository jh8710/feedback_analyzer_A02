package com.example.demo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final String SENTIMENT_POSITIVE = "긍정";
    public static final String SENTIMENT_NEUTRAL = "중립";
    public static final String SENTIMENT_NEGATIVE = "부정";

    public static final String CATEGORY_DELIVERY = "배송";
    public static final String CATEGORY_QUALITY = "품질";
    public static final String CATEGORY_PRICE = "가격";
    public static final String CATEGORY_SERVICE = "서비스";
    public static final String CATEGORY_USABILITY = "사용성";

    public static final String CATEGORY_MAIN_KEY = "main";
    public static final String CATEGORY_SUB_KEY = "sub";

    public static final int INITIAL_ANALYSIS_COUNT = 0;

    public static final Map<String, List<String>> SENTIMENT_KEYWORDS = new HashMap<>();
    public static final Map<String, Map<String, Object>> CATEGORY_KEYWORDS = new HashMap<>();

    static {
        // Sentiment keywords
        SENTIMENT_KEYWORDS.put(SENTIMENT_POSITIVE, Arrays.asList(
                "좋아요", "만족", "감사", "최고", "좋은", "훌륭", "추천", "좋았어요",
                "좋습니다", "최고입니다", "감사합니다", "만족스럽", "좋았습니다",
                "최고에요", "기뻐요", "만족합니다", "굿", "최고다", "와우", "아주 좋아",
                "좋아요", "만족", "감사", "최고", "좋은", "훌륭", "추천", "좋았어요",
                "좋습니다", "최고입니다", "감사합니다", "만족스럽", "좋았습니다"
        ));

        SENTIMENT_KEYWORDS.put(SENTIMENT_NEGATIVE, Arrays.asList(
                "나쁘", "불만", "실망", "최악", "별로", "불편", "불만족", "문제",
                "불량", "불량품", "환불", "교환", "불만족스럽", "실망스럽",
                "짜증", "화남", "별로에요", "엉망", "최악이다", "실패", "구려",
                "나쁘", "불만", "실망", "최악", "별로", "불편", "불만족", "문제",
                "불량", "불량품", "환불", "교환", "불만족스럽", "실망스럽"
        ));

        // Category keywords
        Map<String, Object> deliveryKeywords = new HashMap<>();
        deliveryKeywords.put(CATEGORY_MAIN_KEY, Arrays.asList("배송", "택배", "배달", "물류", "배송지연", "배송시간", "퀵", "소포"));
        Map<String, List<String>> deliverySubcategoryKeywords = new HashMap<>();
        deliverySubcategoryKeywords.put("time", Arrays.asList("배송지연", "배송시간", "퀵"));
        deliverySubcategoryKeywords.put("type", Arrays.asList("택배", "배달", "소포"));
        deliverySubcategoryKeywords.put("status", Arrays.asList("물류", "배송"));
        deliveryKeywords.put(CATEGORY_SUB_KEY, deliverySubcategoryKeywords);
        CATEGORY_KEYWORDS.put(CATEGORY_DELIVERY, deliveryKeywords);

        Map<String, Object> qualityKeywords = new HashMap<>();
        qualityKeywords.put(CATEGORY_MAIN_KEY, Arrays.asList("품질", "재질", "내구성", "마감", "제품상태", "품질문제", "내용물", "고장"));
        Map<String, List<String>> qualitySubcategoryKeywords = new HashMap<>();
        qualitySubcategoryKeywords.put("physical", Arrays.asList("재질", "내구성", "마감"));
        qualitySubcategoryKeywords.put("state", Arrays.asList("제품상태", "품질문제", "고장"));
        qualitySubcategoryKeywords.put("content", Arrays.asList("내용물"));
        qualityKeywords.put(CATEGORY_SUB_KEY, qualitySubcategoryKeywords);
        CATEGORY_KEYWORDS.put(CATEGORY_QUALITY, qualityKeywords);

        Map<String, Object> priceKeywords = new HashMap<>();
        priceKeywords.put(CATEGORY_MAIN_KEY, Arrays.asList("가격", "비용", "할인", "가성비", "가격대", "비싸", "저렴", "금액", "요금"));
        Map<String, List<String>> priceSubcategoryKeywords = new HashMap<>();
        priceSubcategoryKeywords.put("amount", Arrays.asList("가격", "비용", "금액", "요금"));
        priceSubcategoryKeywords.put("discount", Arrays.asList("할인", "가성비", "가격대"));
        priceSubcategoryKeywords.put("evaluation", Arrays.asList("비싸", "저렴"));
        priceKeywords.put(CATEGORY_SUB_KEY, priceSubcategoryKeywords);
        CATEGORY_KEYWORDS.put(CATEGORY_PRICE, priceKeywords);

        Map<String, Object> serviceKeywords = new HashMap<>();
        serviceKeywords.put(CATEGORY_MAIN_KEY, Arrays.asList("서비스", "응대", "상담", "문의", "답변", "고객서비스", "친절", "불친절"));
        Map<String, List<String>> serviceSubcategoryKeywords = new HashMap<>();
        serviceSubcategoryKeywords.put("interaction", Arrays.asList("응대", "상담", "문의", "답변"));
        serviceSubcategoryKeywords.put("quality", Arrays.asList("친절", "불친절"));
        serviceSubcategoryKeywords.put("type", Arrays.asList("서비스", "고객서비스"));
        serviceKeywords.put(CATEGORY_SUB_KEY, serviceSubcategoryKeywords);
        CATEGORY_KEYWORDS.put(CATEGORY_SERVICE, serviceKeywords);

        Map<String, Object> usabilityKeywords = new HashMap<>();
        usabilityKeywords.put(CATEGORY_MAIN_KEY, Arrays.asList("사용", "편리", "불편", "사용법", "설명서", "사용방법", "어렵", "쉽게"));
        Map<String, List<String>> usabilitySubcategoryKeywords = new HashMap<>();
        usabilitySubcategoryKeywords.put("ease", Arrays.asList("편리", "불편", "쉽게", "어렵"));
        usabilitySubcategoryKeywords.put("guide", Arrays.asList("사용법", "설명서", "사용방법"));
        usabilitySubcategoryKeywords.put("action", Arrays.asList("사용"));
        usabilityKeywords.put(CATEGORY_SUB_KEY, usabilitySubcategoryKeywords);
        CATEGORY_KEYWORDS.put(CATEGORY_USABILITY, usabilityKeywords);
    }
}
