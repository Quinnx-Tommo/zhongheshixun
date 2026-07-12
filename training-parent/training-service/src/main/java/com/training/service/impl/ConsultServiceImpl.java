package com.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.training.common.entity.ConsultRecord;
import com.training.common.entity.KnowledgeBase;
import com.training.mapper.ConsultRecordMapper;
import com.training.mapper.KnowledgeBaseMapper;
import com.training.service.ConsultService;
import com.training.service.ai.LongCatAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 咨询服务实现
 *
 * 智能问答算法：
 * 1. 提取关键词（split by [,\\s]+ 和中文逗号）
 * 2. 查 knowledge_base where keywords like %{任意 keyword}% 或 question like %{keyword}%
 * 3. 命中最多的条目作为最佳匹配
 * 4. 命中则返回 autoReply，matched=true
 * 5. 未命中则调用 LongCat AI 接口获取智能回答
 * 6. LongCat 也未返回则创建人工工单（answer=null），matched=false
 */
@Slf4j
@Service
public class ConsultServiceImpl implements ConsultService {

    /** 关键词分隔符：中英文逗号、空格、制表符等 */
    private static final Pattern KEYWORD_SPLIT = Pattern.compile("[,，\\s]+");

    @Resource
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Resource
    private ConsultRecordMapper consultRecordMapper;

    @Resource
    private LongCatAiService longCatAiService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AskResult ask(Long userId, String question) {
        if (!StringUtils.hasText(question)) {
            throw new IllegalArgumentException("问题不能为空");
        }

        // 1. 提取关键词（同时使用整词和分词）
        List<String> keywordList = extractKeywords(question);
        log.info("用户[{}]提问: {}, 提取关键词: {}", userId, question, keywordList);

        // 2. 知识库关键词匹配
        KnowledgeBase bestMatch = null;
        if (!keywordList.isEmpty()) {
            String[] keywords = keywordList.toArray(new String[0]);
            List<KnowledgeBase> matched = knowledgeBaseMapper.matchByKeywords(keywords);
            if (matched != null && !matched.isEmpty()) {
                bestMatch = matched.get(0);
            }
        }

        // 3. 保存咨询记录
        ConsultRecord record = new ConsultRecord();
        record.setStudentId(userId);
        record.setQuestion(question);

        if (bestMatch != null) {
            // 命中知识库 -> 自动回复
            record.setAnswer(bestMatch.getAnswer());
            record.setIsAuto(1);
            record.setReplyTime(LocalDateTime.now());
            consultRecordMapper.insert(record);
            log.info("用户[{}]提问命中知识库[id={}], 自动回复", userId, bestMatch.getId());
            return new AskResult(record.getId(), bestMatch.getAnswer(), true, "kb");
        }

        // 4. 知识库未命中 -> 调用 LongCat AI
        String aiAnswer = longCatAiService.ask(question);
        if (StringUtils.hasText(aiAnswer)) {
            record.setAnswer(aiAnswer);
            record.setIsAuto(1);
            record.setReplyTime(LocalDateTime.now());
            consultRecordMapper.insert(record);
            log.info("用户[{}]提问由 LongCat AI 自动回复, consultId={}", userId, record.getId());
            return new AskResult(record.getId(), aiAnswer, true, "ai");
        }

        // 5. LongCat 也未返回 -> 创建人工工单
        record.setIsAuto(0);
        consultRecordMapper.insert(record);
        log.info("用户[{}]提问未命中知识库且 LongCat 无回复, 创建人工工单[id={}]", userId, record.getId());
        return new AskResult(record.getId(), null, false, "human");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reply(Long consultId, String answer) {
        if (consultId == null) {
            throw new IllegalArgumentException("咨询ID不能为空");
        }
        if (!StringUtils.hasText(answer)) {
            throw new IllegalArgumentException("回复内容不能为空");
        }
        ConsultRecord exist = consultRecordMapper.selectById(consultId);
        if (exist == null) {
            throw new IllegalArgumentException("咨询记录不存在");
        }
        int rows = consultRecordMapper.updateReply(consultId, answer, 2, LocalDateTime.now());
        if (rows <= 0) {
            throw new IllegalStateException("回复失败，请重试");
        }
        log.info("管理员回复咨询[id={}]", consultId);
    }

    @Override
    public List<ConsultRecord> getOverdueConsults(int slaHours) {
        if (slaHours <= 0) {
            slaHours = 24;
        }
        return consultRecordMapper.selectOverdue(slaHours);
    }

    @Override
    public IPage<ConsultRecord> page(String keyword, Integer isAuto, int pageNum, int pageSize) {
        Page<ConsultRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ConsultRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ConsultRecord::getQuestion, keyword);
        }
        if (isAuto != null) {
            wrapper.eq(ConsultRecord::getIsAuto, isAuto);
        }
        // 未回复的排在前面（便于管理员优先处理）
        wrapper.orderByAsc(ConsultRecord::getReplyTime);
        wrapper.orderByDesc(ConsultRecord::getCreateTime);
        return consultRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<ConsultRecord> myList(Long studentId, int pageNum, int pageSize) {
        Page<ConsultRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ConsultRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultRecord::getStudentId, studentId);
        wrapper.orderByDesc(ConsultRecord::getCreateTime);
        return consultRecordMapper.selectPage(page, wrapper);
    }

    /**
     * 从问题中提取关键词列表
     * 策略：
     * 1. 先按中英文逗号、空格等分隔符拆分
     * 2. 如果只有一个长字符串（中文常见），则同时提取 2-4 字的滑动窗口子串
     *    这样 "考试没通过可以重考吗" 会生成 "考试","试没","没通"... "重考" 等子串
     * 3. 同时保留整词作为 keyword（用于 question LIKE 匹配）
     */
    private List<String> extractKeywords(String question) {
        if (!StringUtils.hasText(question)) {
            return java.util.Collections.emptyList();
        }

        Set<String> result = new LinkedHashSet<>();
        String trimmed = question.trim();

        // 1. 按分隔符拆分
        String[] parts = KEYWORD_SPLIT.split(trimmed);

        for (String part : parts) {
            if (!StringUtils.hasText(part)) continue;
            part = part.trim();
            result.add(part);

            // 2. 如果是中文长字符串（>= 4 字），生成 2-3 字滑动窗口
            if (part.length() >= 4) {
                for (int len = 2; len <= 3; len++) {
                    for (int i = 0; i + len <= part.length(); i++) {
                        result.add(part.substring(i, i + len));
                    }
                }
            }
        }

        return new ArrayList<>(result);
    }
}