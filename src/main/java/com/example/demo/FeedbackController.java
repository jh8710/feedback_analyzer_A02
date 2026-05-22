package com.example.demo;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class FeedbackController {
    @Autowired
    private UIComponents uiComponents;

    @Autowired
    private Logger logger;

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/")
    public String index(Model model) {
        List<Feedback> feedbacks = feedbackService.getFeedbacksForIndex();

        model.addAttribute("success", "피드백 분석기 시작");
        model.addAttribute("feedbacks", feedbacks);
        addCommonModelAttributes(model);

        return "index";
    }

    @PostMapping("/analyze")
    public String analyze(@RequestParam("text") String text, Model model) {
        try {
            FeedbackService.AnalysisResult result = feedbackService.addAndAnalyze(text);

            model.addAttribute("success", result.getFeedbacks().size() + "개의 피드백이 입력되었습니다.");
            model.addAttribute("feedbacks", result.getFeedbacks());
            if (result.hasResults()) {
                model.addAttribute("sentimentResults", result.getSentimentResults());
                model.addAttribute("keywordResults", result.getKeywordResults());
            }

        } catch (Exception e) {
            logger.logError("오류 발생: %s", e.getMessage());
            model.addAttribute("error", "처리 중 오류가 발생했습니다.");
        }

        addCommonModelAttributes(model);
        return "index";
    }

    @PostMapping("/log-level")
    public String updateLogLevel(@RequestParam("logLevel") String logLevel, Model model) {
        try {
            feedbackService.updateLogLevel(logLevel);
            model.addAttribute("success", "로그 레벨이 " + feedbackService.getLogLevel() + "(으)로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "지원하지 않는 로그 레벨입니다.");
        }

        addCommonModelAttributes(model);
        return "index";
    }


    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            if (!file.isEmpty()) {
                List<Feedback> feedbacks = feedbackService.uploadFeedbacks(file);
                model.addAttribute("success", feedbacks.size() + "개의 피드백이 입력되었습니다.");
                model.addAttribute("feedbacks", feedbacks);
            }
        } catch (Exception e) {
            logger.logError("파일 업로드 오류: %s", e.getMessage());
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다.");
        }

        addCommonModelAttributes(model);
        return "index";
    }

    @PostMapping("/filter")
    public String filter(@RequestParam("sentiment") String sentiment,
                         @RequestParam("keyword") String keyword,
                         Model model) {
        try {
            FeedbackService.FilterResult result = feedbackService.filterFeedbacks(sentiment, keyword);

            if (result.hasResults()) {
                model.addAttribute("sentimentResults", result.getSentimentResults());
                model.addAttribute("keywordResults", result.getKeywordResults());
                model.addAttribute("filteredFeedbacks", result.getFilteredFeedbacks());
            } else {
                model.addAttribute("warning", result.getWarning());
            }

        } catch (Exception e) {
            logger.logError("오류 발생: %s", e.getMessage());
            model.addAttribute("error", "처리 중 오류가 발생했습니다.");
        }

        addCommonModelAttributes(model);
        return "index";
    }

    @GetMapping("/download")
    public void downloadFile(HttpServletResponse res) throws IOException {
        String fn = "filtered_feedback.csv; charset=UTF-8";

        res.setContentType("text/csv");
        res.setHeader("Content-Disposition", "attachment:filename=\"" + fn + "\"");

        byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF}; // UTF-8 BOM
        res.getOutputStream().write(bom);

        PrintWriter wr =  new PrintWriter(res.getOutputStream(), true, StandardCharsets.UTF_8);

        wr.println("text");
        for(Feedback iter : feedbackService.getFilteredFeedbacks()) {
            wr.println(iter.getText());
        }
        wr.flush();
        wr.close();
    }

    private void addCommonModelAttributes(Model model) {
        model.addAttribute("categories", uiComponents.getCategories());
        model.addAttribute("logLevel", logger.getLogLevel());
        model.addAttribute("trendPoints", feedbackService.getTrendPoints());
    }
}
