package com.training.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.CoursePageQuery;
import com.training.common.entity.Course;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程 Mapper
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 分页查询课程列表（支持按标题、类型、状态筛选）
     */
    IPage<Course> selectCoursePage(IPage<Course> page,
                                   @Param("title") String title,
                                   @Param("courseType") Integer courseType,
                                   @Param("status") Integer status);

    /**
     * 查询学员已报名的课程列表
     */
    IPage<Course> selectEnrolledCourses(IPage<Course> page, @Param("studentId") Long studentId);

    /**
     * 推荐课程（按报名数降序，仅已发布，Top-N）
     */
    List<Course> selectRecommend(@Param("limit") int limit);
}
