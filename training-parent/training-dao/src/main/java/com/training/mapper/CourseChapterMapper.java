package com.training.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.training.common.entity.CourseChapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程章节 Mapper
 */
@Mapper
public interface CourseChapterMapper extends BaseMapper<CourseChapter> {

    /**
     * 分页查询章节列表（按课程ID，可选标题模糊查询）
     */
    IPage<CourseChapter> selectChapterPage(IPage<CourseChapter> page,
                                           @Param("courseId") Long courseId,
                                           @Param("title") String title);
}
