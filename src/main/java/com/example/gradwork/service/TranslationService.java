package com.example.gradwork.service;

import com.example.gradwork.config.ApiConfig;
import com.example.gradwork.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class TranslationService {

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private RestTemplate restTemplate;

    private final String BAIDU_API_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate";
    private final Random random = new Random();

    // 支持的语言映射
    private final Map<String, String> supportedLanguages = new HashMap<String, String>() {{
        put("zh", "中文");
        put("en", "英语");
        put("jp", "日语");
        put("kor", "韩语");
        put("fra", "法语");
        put("spa", "西班牙语");
        put("ru", "俄语");
        put("de", "德语");
        // 更多语言...
    }};

    public String translate(String text, String fromLanguage, String toLanguage) {
        try {
            // 处理相同语言的情况
            if (fromLanguage.equals(toLanguage)) {
                return text;
            }

            // 生成随机数
            String salt = String.valueOf(random.nextInt(10000));

            // 构建签名
            String sign = generateSign(apiConfig.getBaiduAppId(), text, salt, apiConfig.getBaiduSecurityKey());

            // 构建请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("q", text);
            params.add("from", fromLanguage);
            params.add("to", toLanguage);
            params.add("appid", apiConfig.getBaiduAppId());
            params.add("salt", salt);
            params.add("sign", sign);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            // 发送请求
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    BAIDU_API_URL,
                    requestEntity,
                    Map.class
            );

            // 处理响应
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("trans_result")) {
                Object transResult = responseBody.get("trans_result");
                if (transResult instanceof List<?>) {
                    List<?> results = (List<?>) transResult;
                    for (Object result : results) {
                        if (result instanceof Map) {
                            @SuppressWarnings("unchecked")  // 明确抑制警告
                            Map<String, String> resultMap = (Map<String, String>) result;
                            if (resultMap.containsKey("dst")) {
                                return resultMap.get("dst");
                            }
                        }
                    }
                }
            }

            throw new ApiException("Translation failed: Invalid response format");
        } catch (Exception e) {
            throw new ApiException("Translation failed: " + e.getMessage(), e);
        }
    }

    public String detectLanguage(String text) {
        try {
            // 使用百度翻译API的语言检测功能
            String salt = String.valueOf(random.nextInt(10000));
            String sign = generateSign(apiConfig.getBaiduAppId(), text, salt, apiConfig.getBaiduSecurityKey());

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("q", text);
            params.add("appid", apiConfig.getBaiduAppId());
            params.add("salt", salt);
            params.add("sign", sign);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://fanyi-api.baidu.com/api/trans/vip/language",
                    requestEntity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                Map<String, String> data = (Map<String, String>) responseBody.get("data");
                if (data.containsKey("src")) {
                    return data.get("src");
                }
            }

            // 默认返回英语
            return "en";
        } catch (Exception e) {
            // 如果API调用失败，默认返回英语
            return "en";
        }
    }

    public Map<String, String> getSupportedLanguages() {
        return new HashMap<>(supportedLanguages);
    }

    private String generateSign(String appId, String query, String salt, String securityKey) {
        try {
            String str = appId + query + salt + securityKey;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));

            StringBuilder sign = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sign.append("0");
                }
                sign.append(hex);
            }
            return sign.toString();
        } catch (Exception e) {
            throw new ApiException("Failed to generate sign: " + e.getMessage(), e);
        }
    }
}
