package com.training.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.training.common.dto.PlanCourseAddDTO;
import com.training.common.dto.TrainPlanForm;
import com.training.common.dto.TrainPlanPageQuery;
import com.training.common.entity.Course;
import com.training.common.entity.TrainPlan;
import com.training.common.entity.TrainPlanCourse;
import com.training.common.result.Result;
import com.training.mapper.CourseMapper;
import com.training.service.TrainPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 培训计划管理控制器（后台管理端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/train-plan")
public class TrainPlanController {

    @Resource
    private TrainPlanService trainPlanService;

    @Resource
    private CourseMapper courseMapper;

    /**
     * 分页列表
     * 返回 Page（具体类）而非 IPage（接口），避免 Jackson 反序列化歧义
     */
    @PreAuthorize("hasAuthority('plan:read')")
    @GetMapping("/page")
    public Result<Page<TrainPlan>> page(TrainPlanPageQuery query) {
        Page<TrainPlan> result = trainPlanService.page(query);
        return Result.success(result);
    }

    /**
     * 创建培训计划
     */
    @PreAuthorize("hasAuthority('plan:write')")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid TrainPlanForm form) {
        boolean ok = trainPlanService.create(form);
        return Result.success(ok);
    }

    /**
     * 编辑培训计划
     */
    @PreAuthorize("hasAuthority('plan:write')")
    @PutMapping
    public Result<Boolean> update(@RequestBody @Valid TrainPlanForm form) {
        boolean ok = trainPlanService.updatePlan(form);
        return Result.success(ok);
    }

    /**
     * 删除培训计划（逻辑删除）
     */
    @PreAuthorize("hasAuthority('plan:write')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = trainPlanService.removeById(id);
        return Result.success(ok);
    }

    /**
     * 培训计划详情（含已关联课程列表 + 联表回填 courseName）
     *
     * <p>M12 修复：原实现直接返回 {@code List<TrainPlanCourse>}，前端
     * detail.vue 表格中"课程名称"列读取 {@code courseName} 字段为 undefined，
     * 用户看到的是"已关联列表为空"。此处改为：先取关联记录，再批量联表查
     * course 表，回填 courseName/title/coverUrl/courseType/totalHours，
     * 以 Map 形式返回，前端可直接读 {@code row.courseName / row.title}。</p>
     */
    @PreAuthorize("hasAuthority('plan:read')")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        TrainPlan plan = trainPlanService.getDetail(id);
        List<TrainPlanCourse> relations = trainPlanService.listPlanCourses(id);
        // 联表回填课程信息
        List<Map<String, Object>> courses = new ArrayList<>();
        if (relations != null && !relations.isEmpty()) {
            List<Long> courseIds = relations.stream()
                    .map(TrainPlanCourse::getCourseId)
                    .collect(Collectors.toList());
            List<Course> courseList = courseMapper.selectBatchIds(courseIds);
            Map<Long, Course> courseMap = new HashMap<>();
            if (courseList != null) {
                for (Course c : courseList) {
                    courseMap.put(c.getId(), c);
                }
            }
            for (TrainPlanCourse rel : relations) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rel.getId());
                row.put("planId", rel.getPlanId());
                row.put("courseId", rel.getCourseId());
                row.put("sortOrder", rel.getSortOrder());
                row.put("isRequired", rel.getIsRequired());
                Course c = courseMap.get(rel.getCourseId());
                if (c != null) {
                    row.put("courseName", c.getTitle());
                    row.put("title", c.getTitle());
                    row.put("coverUrl", c.getCoverUrl());
                    row.put("courseType", c.getCourseType());
                    row.put("totalHours", c.getTotalHours());
                } else {
                    row.put("courseName", "课程#" + rel.getCourseId());
                }
                courses.add(row);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("plan", plan);
        data.put("courses", courses);
        return Result.success(data);
    }

    /**
     * 关联课程到计划
     */
    @PreAuthorize("hasAuthority('plan:write')")
    @PostMapping("/courses")
    public Result<Boolean> addCourses(@RequestBody @Valid PlanCourseAddDTO dto) {
        boolean ok = trainPlanService.addCourses(dto);
        return Result.success(ok);
    }

    /**
     * 移除计划关联课程
     */
    @PreAuthorize("hasAuthority('plan:write')")
    @DeleteMapping("/courses/{id}")
    public Result<Boolean> removeCourse(@PathVariable Long id) {
        boolean ok = trainPlanService.removeCourse(id);
        return Result.success(ok);
    }
}
