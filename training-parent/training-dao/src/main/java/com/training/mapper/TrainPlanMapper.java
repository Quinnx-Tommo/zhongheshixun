package com.training.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.entity.TrainPlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 培训计划 Mapper
 */
@Mapper
public interface TrainPlanMapper extends BaseMapper<TrainPlan> {

    /**
     * 分页查询培训计划（支持按计划名称、状态筛选）
     */
    IPage<TrainPlan> selectTrainPlanPage(IPage<TrainPlan> page,
                                         @Param("title") String title,
                                         @Param("status") Integer status);
}
