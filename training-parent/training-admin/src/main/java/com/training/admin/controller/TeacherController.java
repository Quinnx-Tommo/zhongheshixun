package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.TeacherPageQuery;
import com.training.common.entity.Teacher;
import com.training.common.result.Result;
import com.training.service.TeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 讲师管理控制器（后台管理端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/teacher")
public class TeacherController {

    @Resource
    private TeacherService teacherService;

    /**
     * 分页列表
     */
    @PreAuthorize("hasAuthority('teacher:read')")
    @GetMapping("/page")
    public Result<IPage<Teacher>> page(TeacherPageQuery query) {
        IPage<Teacher> result = teacherService.page(query);
        return Result.success(result);
    }

    /**
     * 新增讲师
     */
    @PreAuthorize("hasAuthority('teacher:write')")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid Teacher teacher) {
        boolean ok = teacherService.create(teacher);
        return Result.success(ok);
    }

    /**
     * 编辑讲师
     */
    @PreAuthorize("hasAuthority('teacher:write')")
    @PutMapping
    public Result<Boolean> update(@RequestBody @Valid Teacher teacher) {
        boolean ok = teacherService.updateTeacher(teacher);
        return Result.success(ok);
    }

    /**
     * 删除讲师（逻辑删除）
     */
    @PreAuthorize("hasAuthority('teacher:write')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = teacherService.removeById(id);
        return Result.success(ok);
    }

    /**
     * 讲师详情
     */
    @PreAuthorize("hasAuthority('teacher:read')")
    @GetMapping("/{id}")
    public Result<Teacher> detail(@PathVariable Long id) {
        Teacher teacher = teacherService.getById(id);
        if (teacher == null) {
            return Result.error(404, "讲师不存在");
        }
        return Result.success(teacher);
    }
}
