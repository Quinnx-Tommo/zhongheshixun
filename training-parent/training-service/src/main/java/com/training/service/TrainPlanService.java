package com.training.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.training.common.dto.PlanCourseAddDTO;
import com.training.common.dto.TrainPlanForm;
import com.training.common.dto.TrainPlanPageQuery;
import com.training.common.entity.TrainPlan;
import com.training.common.entity.TrainPlanCourse;

import java.util.List;

/**
 * 培训计划服务接口
 */
public interface TrainPlanService extends IService<TrainPlan> {

    /**
     * 分页查询培训计划（返回具体类 Page，避免 Jackson 对 IPage 接口反序列化歧义）
     */
    Page<TrainPlan> page(TrainPlanPageQuery query);

    /**
     * 创建培训计划
     */
    boolean create(TrainPlanForm form);

    /**
     * 编辑培训计划
     */
    boolean updatePlan(TrainPlanForm form);

    /**
     * 查询计划详情（含关联课程列表）
     */
    TrainPlan getDetail(Long id);

    /**
     * 查询计划已关联的课程
     */
    List<TrainPlanCourse> listPlanCourses(Long planId);

    /**
     * 关联课程到计划
     */
    boolean addCourses(PlanCourseAddDTO dto);

    /**
     * 移除计划关联课程
     */
    boolean removeCourse(Long id);
}
