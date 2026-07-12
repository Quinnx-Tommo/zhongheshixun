package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.CourseDTO;
import com.training.common.dto.CoursePageQuery;
import com.training.common.entity.Course;
import com.training.common.entity.CourseChapter;
import com.training.common.result.Result;
import com.training.mapper.CourseChapterMapper;
import com.training.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程管理控制器（后台管理端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/course")
public class CourseController {

    @Resource
    private CourseService courseService;

    @Resource
    private CourseChapterMapper chapterMapper;

    /**
     * 分页列表
     */
    @PreAuthorize("hasAuthority('course:read')")
    @GetMapping("/page")
    public Result<IPage<Course>> page(CoursePageQuery query) {
        IPage<Course> result = courseService.page(query);
        return Result.success(result);
    }

    /**
     * 新增课程
     */
    @PreAuthorize("hasAuthority('course:write')")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid CourseDTO dto) {
        boolean ok = courseService.create(dto);
        return Result.success(ok);
    }

    /**
     * 编辑课程
     */
    @PreAuthorize("hasAuthority('course:write')")
    @PutMapping
    public Result<Boolean> update(@RequestBody @Valid CourseDTO dto) {
        boolean ok = courseService.update(dto);
        return Result.success(ok);
    }

    /**
     * 发布/下架课程
     */
    @PreAuthorize("hasAuthority('course:write')")
    @PutMapping("/publish")
    public Result<Boolean> publish(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        boolean ok = courseService.publish(id, status);
        return Result.success(ok);
    }

    /**
     * 删除课程（逻辑删除）
     */
    @PreAuthorize("hasAuthority('course:write')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = courseService.removeById(id);
        return Result.success(ok);
    }

    /**
     * 课程详情（含章节列表）
     */
    @PreAuthorize("hasAuthority('course:read')")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Course course = courseService.getById(id);
        if (course == null) {
            return Result.error(404, "课程不存在");
        }
        // 查询章节列表
        List<CourseChapter> chapters = chapterMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseChapter>()
                        .eq(CourseChapter::getCourseId, id)
                        .orderByAsc(CourseChapter::getSortOrder)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("course", course);
        data.put("chapters", chapters);
        return Result.success(data);
    }
}
