package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.KnowledgePageQuery;
import com.training.common.entity.KnowledgePoint;
import com.training.common.result.Result;
import com.training.service.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 知识点管理控制器（后台管理端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/knowledge")
public class KnowledgeController {

    @Resource
    private KnowledgeService knowledgeService;

    /**
     * 分页列表（按 courseId 查询）
     */
    @PreAuthorize("hasAuthority('knowledge:read')")
    @GetMapping("/page")
    public Result<IPage<KnowledgePoint>> page(KnowledgePageQuery query) {
        IPage<KnowledgePoint> result = knowledgeService.page(query);
        return Result.success(result);
    }

    /**
     * 新增知识点
     */
    @PreAuthorize("hasAuthority('knowledge:write')")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid KnowledgePoint point) {
        boolean ok = knowledgeService.create(point);
        return Result.success(ok);
    }

    /**
     * 编辑知识点
     */
    @PreAuthorize("hasAuthority('knowledge:write')")
    @PutMapping
    public Result<Boolean> update(@RequestBody @Valid KnowledgePoint point) {
        boolean ok = knowledgeService.updatePoint(point);
        return Result.success(ok);
    }

    /**
     * 删除知识点（逻辑删除）
     */
    @PreAuthorize("hasAuthority('knowledge:write')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = knowledgeService.removeById(id);
        return Result.success(ok);
    }

    /**
     * 知识点详情
     */
    @PreAuthorize("hasAuthority('knowledge:read')")
    @GetMapping("/{id}")
    public Result<KnowledgePoint> detail(@PathVariable Long id) {
        KnowledgePoint point = knowledgeService.getById(id);
        if (point == null) {
            return Result.error(404, "知识点不存在");
        }
        return Result.success(point);
    }
}
