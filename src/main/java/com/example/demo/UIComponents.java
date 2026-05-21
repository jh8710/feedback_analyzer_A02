package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class UIComponents {
    private static final String[] CATS = {"배송", "품질", "가격", "서비스", "사용성"};

    public Object getCategories() {
        return Arrays.asList(CATS);
    }
}
