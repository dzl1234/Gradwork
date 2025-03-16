package com.example.gradwork.controller;

import com.example.gradwork.dto.TranslationRequest;
import com.example.gradwork.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/translation")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @PostMapping
    public ResponseEntity<?> translate(@Valid @RequestBody TranslationRequest request) {
        String translatedText = translationService.translate(
                request.getText(),
                request.getFromLanguage(),
                request.getToLanguage()
        );

        Map<String, String> response = new HashMap<>();
        response.put("translatedText", translatedText);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/detect")
    public ResponseEntity<?> detectLanguage(@Valid @RequestBody Map<String, String> request) {
        String text = request.get("text");
        String detectedLanguage = translationService.detectLanguage(text);

        Map<String, String> response = new HashMap<>();
        response.put("detectedLanguage", detectedLanguage);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/supported-languages")
    public ResponseEntity<?> getSupportedLanguages() {
        Map<String, String> languages = translationService.getSupportedLanguages();
        return ResponseEntity.ok(languages);
    }
}
