package com.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.training.common.entity.KnowledgeBase;
import com.training.mapper.KnowledgeBaseMapper;
import com.training.service.KnowledgeBaseService;
import org.springframework.stereotype.Service;

/**
 * 知识库服务实现
 */
@Service
public class KnowledgeBaseServiceImpl
        extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase>
        implements KnowledgeBaseService {

    @Override
    public IPage<KnowledgeBase> page(String keyword, String category, int pageNum, int pageSize) {
        Page<KnowledgeBase> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(KnowledgeBase::getQuestion, keyword)
                    .or().like(KnowledgeBase::getKeywords, keyword)
                    .or().like(KnowledgeBase::getAnswer, keyword));
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(KnowledgeBase::getCategory, category);
        }
        wrapper.orderByDesc(KnowledgeBase::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
}
