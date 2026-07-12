package com.training.common.vo;

import lombok.Data;
import java.util.List;

/**
 * 自动组卷返回结果（管理员模板卷持久化后返回给前端）
 */
@Data
public class GenerateResultVO {

    /** 抽中的题目 ID 列表 */
    private List<Long> questionIds;

    /** 题目总数 */
    private Integer questionCount;
}
