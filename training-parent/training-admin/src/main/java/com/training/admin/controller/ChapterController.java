package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.ChapterPageQuery;
import com.training.common.entity.CourseChapter;
import com.training.common.result.Result;
import com.training.service.ChapterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 课程章节管理控制器（后台管理端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/chapter")
public class ChapterController {

    @Resource
    private ChapterService chapterService;

    /**
     * 分页列表（按 courseId 查询）
     */
    @PreAuthorize("hasAuthority('chapter:read')")
    @GetMapping("/page")
    public Result<IPage<CourseChapter>> page(ChapterPageQuery query) {
        IPage<CourseChapter> result = chapterService.page(query);
        return Result.success(result);
    }

    /**
     * 列出某课程全部章节（给下拉/排序用）
     */
    @PreAuthorize("hasAuthority('chapter:read')")
    @GetMapping("/course/{courseId}")
    public Result<List<CourseChapter>> listByCourseId(@PathVariable Long courseId) {
        List<CourseChapter> list = chapterService.listByCourseId(courseId);
        return Result.success(list);
    }

    /**
     * 新增章节
     */
    @PreAuthorize("hasAuthority('chapter:write')")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid CourseChapter chapter) {
        boolean ok = chapterService.create(chapter);
        return Result.success(ok);
    }

    /**
     * 编辑章节
     */
    @PreAuthorize("hasAuthority('chapter:write')")
    @PutMapping
    public Result<Boolean> update(@RequestBody @Valid CourseChapter chapter) {
        boolean ok = chapterService.updateChapter(chapter);
        return Result.success(ok);
    }

    /**
     * 删除章节（逻辑删除）
     */
    @PreAuthorize("hasAuthority('chapter:write')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = chapterService.removeById(id);
        return Result.success(ok);
    }

    /**
     * 章节排序（按传入ID列表顺序重排 sortOrder）
     */
    @PreAuthorize("hasAuthority('chapter:write')")
    @PutMapping("/sort")
    public Result<Boolean> sort(@RequestBody List<Long> idList) {
        boolean ok = chapterService.resort(idList);
        return Result.success(ok);
    }
}
