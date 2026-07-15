package com.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.training.common.dto.CourseDTO;
import com.training.common.dto.CoursePageQuery;
import com.training.common.entity.Course;

import java.util.List;

/**
 * 课程服务接口
 */
public interface CourseService extends IService<Course> {

    /**
     * 分页查询课程列表
     */
    IPage<Course> page(CoursePageQuery query);

    /**
     * 新增课程
     */
    boolean create(CourseDTO dto);

    /**
     * 编辑课程
     */
    boolean update(CourseDTO dto);

    /**
     * 发布/下架课程
     * @param id 课程ID
     * @param status 目标状态：1发布 2下架
     */
    boolean publish(Long id, Integer status);

    /**
     * 推荐课程（按报名数降序，Top-N，仅已发布）
     * @param limit 取前 N 条，默认 5，上限 20（由实现钳制）
     */
    List<Course> recommend(int limit);
}
