package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.entity.ConsultRecord;
import com.training.common.entity.KnowledgeBase;
import com.training.common.result.PageResult;
import com.training.common.result.Result;
import com.training.service.ConsultService;
import com.training.service.KnowledgeBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 咨询管理控制器（后台管理端）
 *
 * 权限：需 ADMIN role
 */
@Slf4j
@RestController
@RequestMapping("/admin/consult")
public class ConsultController {

    @Resource
    private ConsultService consultService;

    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 咨询分页（keyword/status）
     */
    @PreAuthorize("hasAuthority('consult:read')")
    @GetMapping("/page")
    public Result<PageResult<ConsultRecord>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isAuto,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<ConsultRecord> page = consultService.page(keyword, isAuto, pageNum, pageSize);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    /**
     * 人工回复 { id, reply }
     */
    @PreAuthorize("hasAuthority('consult:write')")
    @PostMapping("/reply")
    public Result<Boolean> reply(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String answer = (String) body.get("reply");
        consultService.reply(id, answer);
        return Result.success(true);
    }

    /**
     * SLA 超时告警列表（默认 24 小时）
     */
    @PreAuthorize("hasAuthority('consult:read')")
    @GetMapping("/sla-alert")
    public Result<List<ConsultRecord>> slaAlert(
            @RequestParam(defaultValue = "24") int slaHours) {
        List<ConsultRecord> list = consultService.getOverdueConsults(slaHours);
        return Result.success(list);
    }

    /**
     * 知识库分页
     */
    @PreAuthorize("hasAuthority('consult:read')")
    @GetMapping("/knowledge-base/list")
    public Result<PageResult<KnowledgeBase>> knowledgePage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<KnowledgeBase> page = knowledgeBaseService.page(keyword, category, pageNum, pageSize);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    /**
     * 新增知识库条目
     */
    @PreAuthorize("hasAuthority('consult:write')")
    @PostMapping("/knowledge-base")
    public Result<Boolean> createKnowledge(@RequestBody @Valid KnowledgeBase kb) {
        boolean ok = knowledgeBaseService.save(kb);
        return Result.success(ok);
    }

    /**
     * 编辑知识库条目
     */
    @PreAuthorize("hasAuthority('consult:write')")
    @PutMapping("/knowledge-base")
    public Result<Boolean> updateKnowledge(@RequestBody @Valid KnowledgeBase kb) {
        if (kb.getId() == null) {
            return Result.error(400, "知识库ID不能为空");
        }
        boolean ok = knowledgeBaseService.updateById(kb);
        return Result.success(ok);
    }

    /**
     * 删除知识库条目
     */
    @PreAuthorize("hasAuthority('consult:write')")
    @DeleteMapping("/knowledge-base/{id}")
    public Result<Boolean> deleteKnowledge(@PathVariable Long id) {
        boolean ok = knowledgeBaseService.removeById(id);
        return Result.success(ok);
    }
}
