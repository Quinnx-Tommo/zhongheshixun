package com.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.training.common.dto.KnowledgePageQuery;
import com.training.common.entity.KnowledgePoint;

/**
 * 知识点服务接口
 */
public interface KnowledgeService extends IService<KnowledgePoint> {

    /**
     * 分页查询知识点列表（按课程ID）
     */
    IPage<KnowledgePoint> page(KnowledgePageQuery query);

    /**
     * 新增知识点
     */
    boolean create(KnowledgePoint point);

    /**
     * 编辑知识点
     */
    boolean updatePoint(KnowledgePoint point);
}
