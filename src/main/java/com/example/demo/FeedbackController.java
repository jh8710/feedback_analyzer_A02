package com.example.demo;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class FeedbackController {
    @Autowired
    private TextAnalyzer textAnalyzer;

    @Autowired
    private Filters filters;

    @Autowired
    private FileHandler fileHandler;

    @Autowired
    private UIComponents uiComponents;

    @Autowired
    private Logger logger;

    private List<Feedback> fil_data = new ArrayList<>();

    @GetMapping("/")
    public String index(Model model) {
        Session.initSessionStateUgly();

        List<Feedback> feedbacks = Session.getOldDataFromSession("current_feedbacks");

        model.addAttribute("success", "피드백 분석기 시작");
        model.addAttribute("feedbacks", feedbacks);
        addCommonModelAttributes(model);

        return "index";
    }

    @PostMapping("/analyze")
    public String analyze(@RequestParam("text") String text, Model model) {
        try {
            List<Feedback> feedbacks = Session.getCurrentFeedbacks();

            // Add new feedback
            if (text != null && !text.trim().isEmpty()) {
                feedbacks.add(new Feedback(text.trim()));
            }

            //test log 나중에 삭제해야함
            for (Feedback feedback : feedbacks) {
                logger.logInfo("%s", feedback.getText());
            }

            Session.updateInternalData("current_feedbacks", feedbacks);
            logger.logInfo("현재 %d개의 피드백이 입력되었습니다.", feedbacks.size());

            model.addAttribute("success", feedbacks.size() + "개의 피드백이 입력되었습니다.");
            // Analyze feedbacks
            if (!feedbacks.isEmpty()) {
                Map<String, Integer> sentimentResults = textAnalyzer.analyzeSentiments(feedbacks);
                Map<String, Integer> keywordResults = textAnalyzer.analyzeCategoryKeywords(feedbacks);

                model.addAttribute("sentimentResults", sentimentResults);
                model.addAttribute("keywordResults", keywordResults);
                model.addAttribute("feedbacks", feedbacks);
                addCommonModelAttributes(model);

                logger.logInfo("감성 분석 완료");
                logger.logInfo("키워드 분석 완료");
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
            logger.setLogLevel(logLevel);
            model.addAttribute("success", "로그 레벨이 " + logger.getLogLevel() + "(으)로 변경되었습니다.");
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
                List<Feedback> feedbacks = Session.getCurrentFeedbacks();

                // Save uploaded file temporarily
                try {
                    File tmpFile = new File("C:\\\\tmp\\" + file.getOriginalFilename());

                    file.transferTo(tmpFile);
                    CSVReader csvReader = new CSVReader(new FileReader(tmpFile));

                    String[] line;
                    csvReader.readNext(); //skip first line
                    while ((line = csvReader.readNext()) != null) {
                        feedbacks.add(new Feedback(line[0]));
                    }
                    csvReader.close();
                    tmpFile.deleteOnExit();
                } catch (Exception e) {
                    logger.logError("csv 파일 처리 오류: %s", e.getMessage());
                    throw new RuntimeException(e);
                }

                Session.updateInternalData("current_feedbacks", feedbacks);
                model.addAttribute("success", feedbacks.size() + "개의 피드백이 입력되었습니다.");
                model.addAttribute("feedbacks", feedbacks);
                addCommonModelAttributes(model);

                logger.logInfo("파일이 성공적으로 업로드되었습니다.");
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
            List<Feedback> feedbacks = Session.getCurrentFeedbacks();

            if (!feedbacks.isEmpty()) {
                List<Feedback> filtered = filters.fil(feedbacks, sentiment, keyword);

                if (!filtered.isEmpty()) {
                    fil_data = filtered;
                    Map<String, Integer> sentimentResults = textAnalyzer.analyzeSentiments(filtered);
                    Map<String, Integer> keywordResults = textAnalyzer.analyzeCategoryKeywords(filtered);

                    model.addAttribute("sentimentResults", sentimentResults);
                    model.addAttribute("keywordResults", keywordResults);
                    model.addAttribute("filteredFeedbacks", filtered);
                    addCommonModelAttributes(model);

                    logger.logInfo("필터링 결과: %d개의 피드백", filtered.size());
                } else {
                    addCommonModelAttributes(model);
                    logger.logWarning("필터링 결과가 없습니다.");
                    model.addAttribute("warning", "필터링 결과가 없습니다.");
                }
            } else {
                logger.logWarning("분석할 피드백이 없습니다.");
                model.addAttribute("warning", "분석할 피드백이 없습니다.");
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
        for(Feedback iter : fil_data) {
            wr.println(iter.getText());
        }
        wr.flush();
        wr.close();
    }

    private void addCommonModelAttributes(Model model) {
        model.addAttribute("categories", uiComponents.getCategories());
        model.addAttribute("logLevel", logger.getLogLevel());
    }
}
