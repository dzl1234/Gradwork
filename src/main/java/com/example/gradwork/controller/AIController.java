package com.example.gradwork.controller;

import com.example.gradwork.dto.AIRequest;
import com.example.gradwork.model.User;
import com.example.gradwork.service.AIService;
import com.example.gradwork.service.TranslationService;
import com.example.gradwork.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private UserService userService;

    @Autowired
    private TranslationService translationService;

    @PostMapping("/ask")
    public ResponseEntity<?> askAI(@Valid @RequestBody AIRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());

        String question = request.getQuestion();
        String detectedLanguage = translationService.detectLanguage(question);

        // 如果用户语言不是中文，且检测到的问题语言也不是中文，则翻译为中文发送给AI
        String questionForAI = question;
        if (!currentUser.getPreferredLanguage().equals("zh") && !detectedLanguage.equals("zh")) {
            questionForAI = translationService.translate(question, detectedLanguage, "zh");
        }

        // 获取AI回答
        String aiResponse = aiService.getAIResponse(questionForAI);

        // 如果用户首选语言不是中文，则将AI回答翻译为用户语言
        String translatedResponse = aiResponse;
        if (!currentUser.getPreferredLanguage().equals("zh")) {
            translatedResponse = translationService.translate(aiResponse, "zh", currentUser.getPreferredLanguage());
        }

        Map<String, String> response = new HashMap<>();
        response.put("answer", translatedResponse);

        return ResponseEntity.ok(response);
    }
}
