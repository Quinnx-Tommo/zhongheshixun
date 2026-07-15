package com.training.api.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.CoursePageQuery;
import com.training.common.entity.Course;
import com.training.common.entity.CourseChapter;
import com.training.common.entity.Teacher;
import com.training.common.result.PageResult;
import com.training.common.result.Result;
import com.training.api.vo.CourseDetailVO;
import com.training.mapper.CourseChapterMapper;
import com.training.mapper.TeacherMapper;
import com.training.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 小程序课程接口（公开，无需登录）
 */
@Slf4j
@RestController
@RequestMapping("/api/course")
public class CourseApiController {

    @Resource
    private CourseService courseService;

    @Resource
    private CourseChapterMapper chapterMapper;

    @Resource
    private TeacherMapper teacherMapper;

    /**
     * 课程列表（仅返回已发布课程）
     *
     * <p>M13 阶段修复：补全每条课程记录的 {@code teacherName}（联表 teacher.realName）。</p>
     * <ul>
     *   <li>先批量收集所有 {@code teacherId}（去重），用 {@code teacherMapper.selectBatchIds}
     *       一次性查询讲师字典，构建 {@code id -> realName} 映射；</li>
     *   <li>再回填到每条 {@code Course.teacherName} 字段（Course 实体已声明
     *       {@code @TableField(exist = false)}，不会污染持久化）。</li>
     * </ul>
     */
    @GetMapping("/list")
    public Result<PageResult<Course>> list(CoursePageQuery q) {
        // 强制只返回已发布课程
        q.setStatus(1);
        IPage<Course> page = courseService.page(q);
        long total = page.getTotal();
        // 修复 selectCoursePage 不设置 total 的兜底
        if (total <= 0 && page.getRecords() != null && !page.getRecords().isEmpty()) {
            total = page.getRecords().size();
        }

        // M13：批量回填 teacherName（抽公共方法，list/recommend 共用）
        fillTeacherName(page.getRecords());

        return Result.success(PageResult.of(page.getRecords(), total,
                (int) page.getCurrent(), (int) page.getSize()));
    }

    /**
     * 课程详情（含章节列表 + 讲师姓名 + 难度等级 + 总学时）。
     *
     * <p>P0 修复（M10 联调期）：整体 try-catch，异常时返回 404 友好提示
     * 避免触发 GlobalExceptionHandler "系统繁忙"（500）。</p>
     */
    @GetMapping("/detail/{id}")
    public Result<CourseDetailVO> detail(@PathVariable Long id) {
        try {
            Course course = courseService.getById(id);
            if (course == null || course.getStatus() == null || course.getStatus() != 1) {
                return Result.error(404, "课程不存在或未发布");
            }
            List<CourseChapter> chapters = chapterMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseChapter>()
                            .eq(CourseChapter::getCourseId, id)
                            .orderByAsc(CourseChapter::getSortOrder)
            );
            // P0-1 修复：向前端 learn/detail 注入 contentType/content（学员端章节支持视频/PDF/文本三类）
            fillChapterContent(chapters);

            // 联表 teacher 取讲师姓名（M13 阶段修复：course 详情页显示"-"问题）
            String teacherName = null;
            if (course.getTeacherId() != null) {
                Teacher teacher = teacherMapper.selectById(course.getTeacherId());
                if (teacher != null) {
                    teacherName = teacher.getRealName();
                    course.setTeacherName(teacherName);
                }
            }

            CourseDetailVO vo = new CourseDetailVO();
            vo.setCourse(course);
            vo.setChapters(chapters);
            vo.setTeacherName(teacherName);
            vo.setTotalHours(course.getTotalHours());
            // M13：把 courseType 平铺为 type，供前端字典映射（修复右侧栏"课程类型: 其他"）
            vo.setType(course.getCourseType());
            return Result.success(vo);
        } catch (Exception e) {
            log.error("course detail failed, id={}", id, e);
            return Result.error(404, "课程不存在或未发布");
        }
    }

    /**
     * 章节列表
     */
    @GetMapping("/chapter/list")
    public Result<List<CourseChapter>> chapterList(@RequestParam Long courseId) {
        List<CourseChapter> chapters = chapterMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseChapter>()
                        .eq(CourseChapter::getCourseId, courseId)
                        .orderByAsc(CourseChapter::getSortOrder)
        );
        // P0-1 修复：同上
        fillChapterContent(chapters);
        return Result.success(chapters);
    }

    /**
     * P0-1 修复：为章节列表注入前端 learn.vue/detail.vue 期望的 contentType/content 字段。
     * <p>当前 course_chapter 表只有 videoUrl，故所有章节默认视为视频类型（contentType=1），
     * content 取 videoUrl。后续若 schema 升级支持 PDF/文本，可在该方法中按字段值路由。</p>
     */
    private void fillChapterContent(List<CourseChapter> chapters) {
        if (chapters == null || chapters.isEmpty()) return;
        for (CourseChapter ch : chapters) {
            if (ch.getContentType() == null) {
                ch.setContentType(1); // 1 = 视频
            }
            if (ch.getContent() == null) {
                ch.setContent(ch.getVideoUrl());
            }
        }
    }

    /**
     * 推荐课程（按报名数降序，仅已发布，公开接口，无需登录）
     */
    @GetMapping("/recommend")
    public Result<List<Course>> recommend(@RequestParam(defaultValue = "5") int limit) {
        List<Course> list = courseService.recommend(limit);
        fillTeacherName(list);
        return Result.success(list);
    }

    /**
     * 批量回填 teacherName（联表 teacher.realName），list/recommend 共用，避免重复代码。
     */
    private void fillTeacherName(List<Course> courses) {
        if (courses == null || courses.isEmpty()) return;
        Set<Long> teacherIds = new HashSet<>();
        for (Course c : courses) {
            if (c.getTeacherId() != null) {
                teacherIds.add(c.getTeacherId());
            }
        }
        if (teacherIds.isEmpty()) return;
        List<Teacher> teachers = teacherMapper.selectBatchIds(teacherIds);
        Map<Long, String> teacherNameMap = teachers.stream()
                .collect(Collectors.toMap(Teacher::getId, Teacher::getRealName, (a, b) -> a));
        for (Course c : courses) {
            if (c.getTeacherId() != null) {
                c.setTeacherName(teacherNameMap.get(c.getTeacherId()));
            }
        }
    }
}
