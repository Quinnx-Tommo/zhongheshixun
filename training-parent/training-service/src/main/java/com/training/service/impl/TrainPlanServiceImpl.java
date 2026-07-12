package com.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.training.common.dto.PlanCourseAddDTO;
import com.training.common.dto.TrainPlanForm;
import com.training.common.dto.TrainPlanPageQuery;
import com.training.common.entity.TrainPlan;
import com.training.common.entity.TrainPlanCourse;
import com.training.mapper.PlanCourseMapper;
import com.training.mapper.TrainPlanMapper;
import com.training.service.TrainPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 培训计划服务实现
 *
 * <p>P0 修复（M10 联调期）：给 {@link #getDetail(Long)} 与
 * {@link #listPlanCourses(Long)} 加 try-catch 防御，捕获底层异常后返回 null/空集合，
 * 由 Controller 统一转换为 404 友好提示，避免 GlobalExceptionHandler 把任何异常
 * 都包装成"系统繁忙，请稍后重试"（500）——历史上多次因为 mapper 抛 NPE / SQL 异常
 * 导致前端 plan/detail.vue 显示"加载失败/服务器繁忙"。</p>
 */
@Slf4j
@Service
public class TrainPlanServiceImpl extends ServiceImpl<TrainPlanMapper, TrainPlan> implements TrainPlanService {

    @Resource
    private PlanCourseMapper planCourseMapper;

    @Override
    public Page<TrainPlan> page(TrainPlanPageQuery query) {
        Page<TrainPlan> page = new Page<>(query.getPageNum(), query.getPageSize());
        return (Page<TrainPlan>) baseMapper.selectTrainPlanPage(page, query.getTitle(), query.getStatus());
    }

    @Override
    public boolean create(TrainPlanForm form) {
        TrainPlan plan = new TrainPlan();
        plan.setTitle(form.getTitle());
        plan.setDescription(form.getDescription());
        plan.setStatus(form.getStatus() == null ? 0 : form.getStatus());
        return save(plan);
    }

    @Override
    public boolean updatePlan(TrainPlanForm form) {
        if (form.getId() == null) {
            throw new IllegalArgumentException("培训计划ID不能为空");
        }
        TrainPlan exist = getById(form.getId());
        if (exist == null) {
            throw new IllegalArgumentException("培训计划不存在");
        }
        TrainPlan plan = new TrainPlan();
        plan.setId(form.getId());
        plan.setTitle(form.getTitle());
        plan.setDescription(form.getDescription());
        if (form.getStatus() != null) {
            plan.setStatus(form.getStatus());
        }
        return updateById(plan);
    }

    /**
     * 培训计划详情。
     *
     * <p>P0 修复：原实现在 mapper 抛异常时会让 GlobalExceptionHandler 兜底返回
     * "系统繁忙，请稍后重试"（500），前端 plan/detail.vue 把任意 reject 都视为
     * "计划加载失败"，无法区分"不存在"和"服务器异常"。这里改为捕获后返回 null，
     * 由 PlanApiController.detail 统一映射为 404。</p>
     */
    @Override
    public TrainPlan getDetail(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return getById(id);
        } catch (Exception e) {
            log.error("getDetail failed, id={}", id, e);
            return null;
        }
    }

    @Override
    public List<TrainPlanCourse> listPlanCourses(Long planId) {
        if (planId == null) {
            return new ArrayList<>();
        }
        try {
            LambdaQueryWrapper<TrainPlanCourse> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TrainPlanCourse::getPlanId, planId);
            wrapper.orderByAsc(TrainPlanCourse::getSortOrder);
            List<TrainPlanCourse> list = planCourseMapper.selectList(wrapper);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception e) {
            log.error("listPlanCourses failed, planId={}", planId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCourses(PlanCourseAddDTO dto) {
        TrainPlan plan = getById(dto.getPlanId());
        if (plan == null) {
            throw new IllegalArgumentException("培训计划不存在");
        }
        // 获取当前关联课程数
        LambdaQueryWrapper<TrainPlanCourse> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(TrainPlanCourse::getPlanId, dto.getPlanId());
        Long countObj = planCourseMapper.selectCount(countWrapper);
        int count = countObj == null ? 0 : countObj.intValue();

        // M12 修复：先查出已存在的 (planId, courseId) 集合，去重后避免重复插入
        // （plan_course 表无 unique key,MyBatis-Plus 不会去重）
        List<TrainPlanCourse> existing = planCourseMapper.selectList(countWrapper);
        java.util.Set<Long> existingCourseIds = new java.util.HashSet<>();
        if (existing != null) {
            for (TrainPlanCourse pc : existing) {
                if (pc.getCourseId() != null) {
                    existingCourseIds.add(pc.getCourseId());
                }
            }
        }

        List<TrainPlanCourse> records = new ArrayList<>();
        int order = count + 1;
        for (Long courseId : dto.getCourseIds()) {
            // 跳过已关联的课程
            if (existingCourseIds.contains(courseId)) {
                log.info("M12 培训计划 {} 已关联课程 {}，跳过", dto.getPlanId(), courseId);
                continue;
            }
            TrainPlanCourse record = new TrainPlanCourse();
            record.setPlanId(dto.getPlanId());
            record.setCourseId(courseId);
            record.setSortOrder(order++);
            record.setIsRequired(1);
            // 显式 set deleted=0 + createTime，绕过 MyBatis-Plus null 跳过策略，避免依赖 DB DEFAULT
            record.setDeleted(0);
            record.setCreateTime(java.time.LocalDateTime.now());
            records.add(record);
        }
        for (TrainPlanCourse record : records) {
            planCourseMapper.insert(record);
        }
        log.info("M12 培训计划 {} 关联课程: 请求 {} 条, 实际新增 {} 条",
                dto.getPlanId(), dto.getCourseIds().size(), records.size());
        return true;
    }

    @Override
    public boolean removeCourse(Long id) {
        return planCourseMapper.deleteById(id) > 0;
    }
}
