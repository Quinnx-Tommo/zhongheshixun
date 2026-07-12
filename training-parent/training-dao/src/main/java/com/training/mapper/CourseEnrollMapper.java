package com.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.training.common.entity.CourseEnroll;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程报名 Mapper
 */
@Mapper
public interface CourseEnrollMapper extends BaseMapper<CourseEnroll> {
}
