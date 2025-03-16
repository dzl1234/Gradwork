package com.example.gradwork.dto;

import lombok.Data;

/**
 * 翻译请求数据传输对象
 */
@Data
public class TranslationRequest {
    private String text; // 要翻译的文本
    private String fromLanguage; // 源语言
    private String toLanguage; // 目标语言
}
