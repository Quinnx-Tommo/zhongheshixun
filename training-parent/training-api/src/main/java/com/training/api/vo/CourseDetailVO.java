package com.training.api.vo;

import com.training.common.entity.Course;
import com.training.common.entity.CourseChapter;
import lombok.Data;

import java.util.List;

/**
 * 课程详情响应 VO（含章节列表）
 *
 * <p>顶层平铺了 {@code course} 实体中常用字段（{@link #teacherName}、
 * {@link #totalHours}）以及章节列表 {@link #chapters}，
 * 前端可直接通过 {@code detail.teacherName / detail.totalHours}
 * 访问，也可继续通过 {@code detail.course.xxx} 兼容旧版本。</p>
 */
@Data
public class CourseDetailVO {

    /** 课程基本信息（包含 teacherName 联表字段） */
    private Course course;

    /** 章节列表 */
    private List<CourseChapter> chapters;

    /** 讲师姓名（联表 teacher.realName）— 平铺字段 */
    private String teacherName;

    /** 总学时 — 平铺字段 */
    private Integer totalHours;

    /**
     * 课程类型（= course.courseType；别名字段）。
     *
     * <p>M13 阶段新增：学员端课程详情右侧栏"课程类型"原读取的是
     * {@code vo.type}，但实体字段名为 {@code courseType}，导致字典翻译失败
     * 显示为"其他"。这里把 {@code courseType} 平铺为 {@code type}，
     * 学员端可直接读 {@code detail.type} 走字典映射。</p>
     *
     * <p>取值：1 公开课 / 2 必修课 / 3 选修课。</p>
     */
    private Integer type;
}
