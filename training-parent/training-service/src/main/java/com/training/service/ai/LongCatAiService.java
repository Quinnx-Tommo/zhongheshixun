package com.training.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.config.LongCatProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * LongCat AI 智能咨询服务
 *
 * 接入 LongCat API 开放平台（兼容 OpenAI /v1/chat/completions 格式）
 * 文档：https://longcat.chat/platform/docs/zh/
 *
 * 使用方式：
 *   1. 在 application.yml 中配置 training.longcat.api-key
 *   2. 设置 training.longcat.enabled=true
 *   3. ConsultServiceImpl 在知识库未命中时自动调用本服务
 */
@Slf4j
@Service
public class LongCatAiService {

    private final LongCatProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    public LongCatAiService(LongCatProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getTimeoutMs());
        factory.setReadTimeout(properties.getTimeoutMs());
        this.restTemplate = new RestTemplate(factory);
        log.info("LongCatAiService 初始化完成: enabled={}, model={}", properties.isEnabled(), properties.getModel());
    }

    /**
     * 调用 LongCat AI 获取回答
     *
     * @param question 用户问题
     * @return AI 回答内容；如果调用失败或未启用则返回 null
     */
    public String ask(String question) {
        if (!properties.isEnabled()) {
            log.debug("LongCat AI disabled, skip");
            return null;
        }
        if (properties.getApiKey() == null || properties.getApiKey().trim().isEmpty()) {
            log.warn("LongCat AI API Key not configured, skip");
            return null;
        }

        try {
            // 构建请求体（OpenAI chat/completions 格式）
            Map<String, Object> body = Map.of(
                    "model", properties.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", properties.getSystemPrompt()),
                            Map.of("role", "user", "content", question)
                    ),
                    "max_tokens", properties.getMaxTokens(),
                    "temperature", properties.getTemperature()
            );

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + properties.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // 发送请求
            String url = properties.getBaseUrl() + "/v1/chat/completions";
            log.info("LongCat AI request: question={}", question);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // 解析响应
            return extractAnswer(response.getBody());
        } catch (Exception e) {
            log.error("LongCat AI call failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 OpenAI 格式的响应中提取回答内容
     *
     * 响应格式：
     * {
     *   "choices": [{ "message": { "content": "回答内容" } }]
     * }
     */
    private String extractAnswer(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                String content = choices.get(0).path("message").path("content").asText();
                if (content != null && !content.isEmpty()) {
                    log.info("LongCat AI response: {} chars", content.length());
                    return content.trim();
                }
            }
            // 检查是否有错误信息
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                log.warn("LongCat AI returned error: {}", error.path("message").asText());
            }
        } catch (Exception e) {
            log.error("Parse LongCat AI response failed: {}", e.getMessage());
        }
        return null;
    }
}