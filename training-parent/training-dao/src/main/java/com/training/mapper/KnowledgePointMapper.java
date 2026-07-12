package com.training.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.training.common.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 知识点 Mapper
 */
@Mapper
public interface KnowledgePointMapper extends BaseMapper<KnowledgePoint> {

    /**
     * 分页查询知识点列表（按课程ID，可选名称模糊查询）
     */
    IPage<KnowledgePoint> selectKnowledgePage(IPage<KnowledgePoint> page,
                                              @Param("courseId") Long courseId,
                                              @Param("name") String name);
}
