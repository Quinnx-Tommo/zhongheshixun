package com.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.training.common.entity.TrainPlanCourse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 计划关联课程 Mapper
 */
@Mapper
public interface PlanCourseMapper extends BaseMapper<TrainPlanCourse> {
}
