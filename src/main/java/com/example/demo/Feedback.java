package com.example.demo;

public class Feedback {

    private String text;

    private String sentiment;
    private String category;

    public Feedback() {}

    public Feedback(String text) {
        this.text = text;
    }

    public Feedback(String text, String sentiment, String category) {
        this.text = text;
        this.sentiment = sentiment;
        this.category = category;
    }

    public String getText() {
        return text;
    }
}
