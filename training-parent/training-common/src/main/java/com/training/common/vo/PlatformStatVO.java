package com.training.common.vo;

import lombok.Data;

/**
 * 平台运行情况统计 VO
 *
 * 用于 /admin/stats/platform 接口，体现 docx 高并发/平台运行监控亮点。
 * - onlineCount: 近 5 分钟有学习心跳上报的去重学员数（在线人数）
 * - todayStudyCount: 今日活跃学员数
 * - concurrentExamCount: 进行中考试数（exam_record.status=0）
 */
@Data
public class PlatformStatVO {

    /** 近 5 分钟活跃学员数（在线人数） */
    private Integer onlineCount;

    /** 今日活跃学员数 */
    private Integer todayStudyCount;

    /** 进行中考试数 */
    private Integer concurrentExamCount;
}
