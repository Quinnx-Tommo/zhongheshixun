package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 咨询记录实体
 */
@Data
@TableName("consult_record")
public class ConsultRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学员ID */
    private Long studentId;

    /** 问题 */
    private String question;

    /** 回答（null 表示待人工回复） */
    private String answer;

    /** 类型：1智能回答 2人工回答 */
    private Integer isAuto;

    private LocalDateTime createTime;

    /** 回复时间（用于 SLA 统计） */
    private LocalDateTime replyTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
