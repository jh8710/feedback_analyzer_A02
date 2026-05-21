package com.example.demo;

import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FileHandler {
    public void saveResult(List<Feedback> data) {
        System.out.println("saveResult" + data.size());
        for(Feedback iter : data)
            System.out.println(iter.getText());

    }

    public void save(List<Feedback> data) {
        saveResult(data);
    }
}
