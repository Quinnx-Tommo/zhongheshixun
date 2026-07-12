package com.training.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LongCat AI 智能咨询配置
 *
 * 接入 LongCat API 开放平台（兼容 OpenAI API 格式）
 * 文档：https://longcat.chat/platform/docs/zh/
 */
@Data
@Component
@ConfigurationProperties(prefix = "training.longcat")
public class LongCatProperties {

    /** 是否启用 LongCat AI 自动回复 */
    private boolean enabled = false;

    /** API Key（从 https://longcat.chat/platform 获取） */
    private String apiKey = "";

    /** API 基础地址（OpenAI 格式） */
    private String baseUrl = "https://api.longcat.chat/openai";

    /** 模型名称 */
    private String model = "LongCat-2.0";

    /** 最大输出 token 数 */
    private int maxTokens = 1000;

    /** 温度（0-1，越低越确定） */
    private double temperature = 0.7;

    /** 请求超时（毫秒） */
    private int timeoutMs = 10000;

    /** 系统提示词（定义 AI 角色） */
    private String systemPrompt = "你是一名基层卫生人员培训平台的智能助手，"
            + "负责回答学员关于课程学习、考试报名、培训安排等问题。"
            + "请用简洁、专业的中文回答，回答不超过 500 字。";
}