package com.example.gradwork.dto;

import lombok.Data;

/**
 * AI问答请求数据传输对象
 */
@Data
public class AIRequest {
    private String question; // 用户问题
    private String language; // 问题语言
    private boolean translateResponse; // 是否需要翻译AI回答
    private String targetLanguage; // 如果需要翻译，则指定目标语言
}
