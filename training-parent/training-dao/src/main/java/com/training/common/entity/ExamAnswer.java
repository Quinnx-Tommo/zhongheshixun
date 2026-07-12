package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 答题记录实体
 */
@Data
@TableName("exam_answer")
public class ExamAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 考试记录ID */
    private Long recordId;

    /** 试题ID */
    private Long questionId;

    /** 学员答案 */
    private String studentAnswer;

    /** 是否正确：0错 1对 null待批阅 */
    private Integer isCorrect;

    /** 得分 */
    private Integer score;

    private LocalDateTime createTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
