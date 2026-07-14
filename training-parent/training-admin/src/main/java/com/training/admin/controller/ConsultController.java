package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.entity.ConsultRecord;
import com.training.common.result.PageResult;
import com.training.common.result.Result;
import com.training.service.ConsultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
}
