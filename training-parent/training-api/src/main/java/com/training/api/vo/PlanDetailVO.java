package com.training.api.vo;

import com.training.common.entity.Course;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 培训计划详情响应 VO（小程序端）
 *
 * <p>注：计划本身不含 startTime/endTime 字段，startTime 用 createTime 兜底，
 * 以便小程序端统一按时间轴渲染。</p>
 */
@Data
public class PlanDetailVO {

    /** 计划ID */
    private Long id;

    /** 计划名称 */
    private String title;

    /** 计划描述 */
    private String description;

    /** 开始时间（取 createTime 兜底） */
    private LocalDateTime startTime;

    /** 结束时间（当前 MVP 无此字段，返回 null） */
    private LocalDateTime endTime;

    /** 关联课程总数 */
    private Integer totalCount;

    /** 已完成课程数（MVP 无学员完成表，固定 0） */
    private Integer completedCount;

    /** 学习进度百分比（0~100，MVP 固定 0） */
    private Integer progress;

    /** 计划关联的课程列表（仅 status=1 已发布课程） */
    private List<Course> courseList;
}
