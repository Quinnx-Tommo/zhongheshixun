package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.QuestionPageQuery;
import com.training.common.entity.Question;
import com.training.common.result.Result;
import com.training.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 试题管理控制器（后台管理端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/question")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    /**
     * 分页列表
     */
    @PreAuthorize("hasAuthority('question:read')")
    @GetMapping("/page")
    public Result<IPage<Question>> page(QuestionPageQuery query) {
        IPage<Question> result = questionService.page(query);
        return Result.success(result);
    }

    /**
     * 新增试题
     */
    @PreAuthorize("hasAuthority('question:write')")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid Question question) {
        boolean ok = questionService.create(question);
        return Result.success(ok);
    }

    /**
     * 编辑试题
     */
    @PreAuthorize("hasAuthority('question:write')")
    @PutMapping
    public Result<Boolean> update(@RequestBody @Valid Question question) {
        boolean ok = questionService.updateQuestion(question);
        return Result.success(ok);
    }

    /**
     * 删除试题（逻辑删除）
     */
    @PreAuthorize("hasAuthority('question:write')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = questionService.removeById(id);
        return Result.success(ok);
    }

    /**
     * 试题详情
     */
    @PreAuthorize("hasAuthority('question:read')")
    @GetMapping("/{id}")
    public Result<Question> detail(@PathVariable Long id) {
        Question question = questionService.getById(id);
        if (question == null) {
            return Result.error(404, "试题不存在");
        }
        return Result.success(question);
    }
}
