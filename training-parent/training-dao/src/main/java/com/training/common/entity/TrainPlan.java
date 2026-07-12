package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 培训计划实体
 */
@Data
@TableName("train_plan")
public class TrainPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 计划名称 */
    private String title;

    /** 计划描述 */
    private String description;

    /** 状态：0草稿 1已发布 2已结束 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
