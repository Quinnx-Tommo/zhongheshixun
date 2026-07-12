package com.training.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程章节实体
 */
@Data
@TableName("course_chapter")
public class CourseChapter {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 章节标题 */
    private String title;

    /** 排序 */
    private Integer sortOrder;

    /** 视频地址 */
    private String videoUrl;

    /** 时长(秒) */
    private Integer duration;

    private LocalDateTime createTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    // ============================================================
    // 以下为非持久化字段（@TableField(exist = false)）
    // 用于向前端 learn.vue / detail.vue 提供 contentType / content 字段
    // （学员端需求"支持视频/PDF/文本三类章节"，当前 schema 仅有 videoUrl，
    // 后续若新增 content_type/content 列，仅需删除 exist=false 即可平滑升级。）
    // ============================================================

    /**
     * 章节内容类型：1 视频 / 2 PDF / 3 文本
     * <p>当前课程章节表只有 videoUrl，故默认 1（视频），由 Controller 注入。</p>
     */
    @TableField(exist = false)
    private Integer contentType;

    /**
     * 章节内容 URL / 文本
     * <p>当前默认与 videoUrl 同值（视频源），由 Controller 注入。</p>
     */
    @TableField(exist = false)
    private String content;
}
