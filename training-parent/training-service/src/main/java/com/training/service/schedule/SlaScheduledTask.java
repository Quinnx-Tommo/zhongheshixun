package com.training.service.schedule;

import com.training.service.ConsultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * SLA 超时定时扫描任务
 *
 * 教师要求：咨询 SLA < 1 分钟（学员提问到老师/系统回复的时间应小于 1 分钟，超时要告警）。
 *
 * 实现策略：
 *  - 每 30 秒扫描一次 consult_record 表
 *  - 把"未回复 且 create_time 距今 >= SLA_MINUTES 分钟"的工单的 sla_exceeded 标记置 1
 *  - 本次新标记的工单数 > 0 时输出 WARN 级别告警日志（管理员可在后台日志或 /admin/consult/sla-alert 看到）
 *
 * 阈值 SLA_MINUTES = 1，与教师要求一致；如需调整改这一处即可。
 */
@Slf4j
@Component
public class SlaScheduledTask {

    /** SLA 阈值（分钟）—— 教师要求 < 1 分钟 */
    private static final int SLA_MINUTES = 1;

    @Resource
    private ConsultService consultService;

    /**
     * 每 30 秒扫描一次（fixedRate 保证间隔稳定，不受上次执行耗时影响）
     *
     * initialDelay = 30 秒：应用启动 30 秒后开始第一次扫描，避免启动高峰
     */
    @Scheduled(fixedRate = 30 * 1000L, initialDelay = 30 * 1000L)
    public void scanSla() {
        try {
            int marked = consultService.markSlaExceeded(SLA_MINUTES);
            if (marked > 0) {
                log.warn("[SLA 告警] 新增 {} 条 {} 分钟超时未回复咨询工单，请管理员尽快处理",
                        marked, SLA_MINUTES);
            }
        } catch (Exception e) {
            // 定时任务异常不能让调度器挂掉，仅记录错误日志
            log.error("[SLA 扫描] 定时任务执行异常: {}", e.getMessage(), e);
        }
    }
}
