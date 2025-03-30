package com.example.gradwork.service;

import com.example.gradwork.config.ApiConfig;
import com.example.gradwork.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AIService {

    @Autowired
    private ApiConfig apiConfig;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 星火大模型API URL
    private final String SPARK_API_URL = "https://spark-api.xf-yun.com/v2.1/chat";

    public String getAIResponse(String question) {
        try {
            // 构建请求体
            ObjectNode requestBody = buildRequestBody(question);

            // 构建完整URL（包含鉴权信息）
            String url = buildAuthUrl();

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");

            // 发送请求
            HttpEntity<String> requestEntity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 检查响应是否有效
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new ApiException("AI service unavailable");
            }

            // 解析响应
            return parseResponse(response.getBody());
        } catch (Exception e) {
            throw new ApiException("AI service error: " + e.getMessage(), e);
        }
    }

    private ObjectNode buildRequestBody(String question) {
        ObjectNode header = JsonNodeFactory.instance.objectNode();
        header.put("app_id", apiConfig.getSparkdeskAppId());

        ObjectNode parameter = JsonNodeFactory.instance.objectNode();
        ObjectNode chat = JsonNodeFactory.instance.objectNode();
        chat.put("domain", "general");
        chat.put("temperature", 0.7);
        chat.put("max_tokens", 1024);
        parameter.set("chat", chat);

        ArrayNode messages = JsonNodeFactory.instance.arrayNode();

        // 用户问题
        ObjectNode userMessage = JsonNodeFactory.instance.objectNode();
        userMessage.put("role", "user");
        userMessage.put("content", question);
        messages.add(userMessage);

        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.set("message", messages);

        ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.set("header", header);
        root.set("parameter", parameter);
        root.set("payload", payload);

        return root;
    }

    private String buildAuthUrl() throws NoSuchAlgorithmException, InvalidKeyException {
        String host = "spark-api.xf-yun.com";
        String path = "/v2.1/chat";

        // API鉴权参数
        String apiKey = apiConfig.getSparkdeskApiKey();
        String apiSecret = apiConfig.getSparkdeskApiSecret();
        String date = getGMTDate();

        // 组织鉴权签名
        String signatureOrigin = String.format("host: %s\ndate: %s\nPOST %s HTTP/1.1", host, date, path);
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(hexDigits);

        // 组织鉴权头
        String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line", signature);
        String authorization = Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));

        // 组织URL
        return String.format("https://%s%s?authorization=%s&date=%s&host=%s",
                host, path,
                URLEncoder.encode(authorization, StandardCharsets.UTF_8),
                URLEncoder.encode(date, StandardCharsets.UTF_8),
                URLEncoder.encode(host, StandardCharsets.UTF_8));
    }

    private String getGMTDate() {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(new Date());
    }

    private String parseResponse(String responseJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode payload = rootNode.path("payload");
            JsonNode choices = payload.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("text").asText();
            }
            throw new ApiException("无效的AI响应格式");
        } catch (Exception e) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AIService.class);
            logger.error("解析失败，原始响应：{}", responseJson); // 添加日志
            throw new ApiException("AI响应解析失败");
        }
    }
}
