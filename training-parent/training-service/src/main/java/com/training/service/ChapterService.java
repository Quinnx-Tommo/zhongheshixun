package com.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.training.common.dto.ChapterPageQuery;
import com.training.common.entity.CourseChapter;

import java.util.List;

/**
 * 课程章节服务接口
 */
public interface ChapterService extends IService<CourseChapter> {

    /**
     * 分页查询章节列表（按课程ID）
     */
    IPage<CourseChapter> page(ChapterPageQuery query);

    /**
     * 列出某课程全部章节（按 sortOrder 升序）
     */
    List<CourseChapter> listByCourseId(Long courseId);

    /**
     * 新增章节
     */
    boolean create(CourseChapter chapter);

    /**
     * 编辑章节
     */
    boolean updateChapter(CourseChapter chapter);

    /**
     * 批量重排章节排序
     *
     * @param idList 按目标顺序排列的章节ID列表
     */
    boolean resort(List<Long> idList);
}
