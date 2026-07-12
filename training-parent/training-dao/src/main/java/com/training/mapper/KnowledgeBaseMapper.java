package com.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.training.common.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库 Mapper
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    /**
     * 根据关键词列表模糊匹配知识库条目
     * 每个 keyword 同时匹配 keywords 字段和 question 字段
     * 按匹配次数降序排列，取最佳匹配
     */
    List<KnowledgeBase> matchByKeywords(@Param("keywords") String[] keywords);
}
