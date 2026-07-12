package com.training.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.training.common.dto.KnowledgePageQuery;
import com.training.common.entity.KnowledgePoint;
import com.training.mapper.KnowledgePointMapper;
import com.training.service.KnowledgeService;
import org.springframework.stereotype.Service;

/**
 * 知识点服务实现
 */
@Service
public class KnowledgeServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint> implements KnowledgeService {

    @Override
    public IPage<KnowledgePoint> page(KnowledgePageQuery query) {
        Page<KnowledgePoint> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectKnowledgePage(page, query.getCourseId(), query.getName());
    }

    @Override
    public boolean create(KnowledgePoint point) {
        return save(point);
    }

    @Override
    public boolean updatePoint(KnowledgePoint point) {
        if (point.getId() == null) {
            throw new IllegalArgumentException("知识点ID不能为空");
        }
        KnowledgePoint exist = getById(point.getId());
        if (exist == null) {
            throw new IllegalArgumentException("知识点不存在");
        }
        return updateById(point);
    }
}
