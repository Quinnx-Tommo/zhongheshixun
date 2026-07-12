package com.training.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.TrainPlanPageQuery;
import com.training.common.entity.Course;
import com.training.common.entity.TrainPlan;
import com.training.common.entity.TrainPlanCourse;
import com.training.common.result.PageResult;
import com.training.common.result.Result;
import com.training.api.vo.PlanDetailVO;
import com.training.service.CourseService;
import com.training.service.TrainPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 小程序培训计划接口（公开，无需登录）
 *
 * <p>对齐 CourseApiController 公开策略：小程序 demo 阶段可匿名访问计划列表。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/plan")
public class PlanApiController {

    @Resource
    private TrainPlanService trainPlanService;

    @Resource
    private CourseService courseService;

    /**
     * 培训计划列表（仅返回已发布的计划，按 createTime 倒序）
     */
    @GetMapping("/list")
    public Result<PageResult<TrainPlan>> list(TrainPlanPageQuery q) {
        // 强制只返回已发布的计划
        q.setStatus(1);
        IPage<TrainPlan> page = trainPlanService.page(q);
        long total = page.getTotal();
        // 修复 selectTrainPlanPage 不设置 total 的兜底
        if (total <= 0 && page.getRecords() != null && !page.getRecords().isEmpty()) {
            total = page.getRecords().size();
        }
        return Result.success(PageResult.of(page.getRecords(), total,
                (int) page.getCurrent(), (int) page.getSize()));
    }

    /**
     * 培训计划详情（含关联课程列表）。
     *
     * <p>P0 修复（M10 联调期）：整体加 try-catch，任何子调用异常都返回
     * "培训计划不存在或未发布"（404）而非 500"系统繁忙"。前端 plan/detail.vue
     * 拿到 404 即进入友好提示页，避免"服务器繁忙"误报。</p>
     */
    @GetMapping("/detail/{id}")
    public Result<PlanDetailVO> detail(@PathVariable Long id) {
        try {
            TrainPlan plan = trainPlanService.getDetail(id);
            if (plan == null || plan.getStatus() == null || plan.getStatus() != 1) {
                return Result.error(404, "培训计划不存在或未发布");
            }

            // 查询计划关联的课程
            List<TrainPlanCourse> rels = trainPlanService.listPlanCourses(id);
            List<Course> courses = Collections.emptyList();
            if (rels != null && !rels.isEmpty()) {
                List<Long> courseIds = rels.stream()
                        .map(TrainPlanCourse::getCourseId)
                        .collect(Collectors.toList());
                List<Course> dbCourses = courseService.listByIds(courseIds);
                if (dbCourses != null) {
                    // 兜底：仅返回已发布的课程，并维持 rels 的 sortOrder 顺序
                    courses = rels.stream()
                            .filter(rel -> dbCourses.stream()
                                    .anyMatch(c -> c.getId().equals(rel.getCourseId())
                                            && c.getStatus() != null && c.getStatus() == 1))
                            .map(rel -> dbCourses.stream()
                                    .filter(c -> c.getId().equals(rel.getCourseId()))
                                    .findFirst().orElse(null))
                            .filter(c -> c != null)
                            .collect(Collectors.toList());
                }
            }

            PlanDetailVO vo = new PlanDetailVO();
            vo.setId(plan.getId());
            vo.setTitle(plan.getTitle());
            vo.setDescription(plan.getDescription());
            // 计划无 startTime/endTime 字段，startTime 用 createTime 兜底
            LocalDateTime fallbackTime = plan.getCreateTime() != null ? plan.getCreateTime() : LocalDateTime.now();
            vo.setStartTime(fallbackTime);
            vo.setEndTime(null);
            vo.setTotalCount(courses.size());
            // MVP：尚无学员-课程完成表，保持 0
            vo.setCompletedCount(0);
            vo.setProgress(0);
            vo.setCourseList(courses);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("plan detail failed, id={}", id, e);
            return Result.error(404, "培训计划不存在或未发布");
        }
    }

    /**
     * "我的培训计划"（MVP 简化实现）
     *
     * <p>当前项目处于游客模式，学员未登录即返回全部 demo 计划，
     * 与 list 接口逻辑一致；后续接入登录态后按 userId 过滤。</p>
     */
    @GetMapping("/my")
    public Result<PageResult<TrainPlan>> my(TrainPlanPageQuery q) {
        return list(q);
    }
}
