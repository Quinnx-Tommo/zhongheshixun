package com.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.training.common.entity.KnowledgeBase;

/**
 * 知识库服务接口
 */
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    /**
     * 分页查询知识库列表
     */
    IPage<KnowledgeBase> page(String keyword, String category, int pageNum, int pageSize);
}
