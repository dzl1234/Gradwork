package com.example.gradwork.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApiConfig {

    @Value("${baidu.translate.appId}")
    private String baiduAppId;

    @Value("${baidu.translate.securityKey}")
    private String baiduSecurityKey;

    @Value("${sparkdesk.ai.appId}")
    private String sparkdeskAppId;

    @Value("${sparkdesk.ai.apiKey}")
    private String sparkdeskApiKey;

    @Value("${sparkdesk.ai.apiSecret}")
    private String sparkdeskApiSecret;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // 百度翻译API配置
    public String getBaiduAppId() {
        return baiduAppId;
    }

    public String getBaiduSecurityKey() {
        return baiduSecurityKey;
    }

    // 星火AI API配置
    public String getSparkdeskAppId() {
        return sparkdeskAppId;
    }

    public String getSparkdeskApiKey() {
        return sparkdeskApiKey;
    }

    public String getSparkdeskApiSecret() {
        return sparkdeskApiSecret;
    }
}
