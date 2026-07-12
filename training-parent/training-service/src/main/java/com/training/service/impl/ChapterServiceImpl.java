package com.training.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.training.common.dto.ChapterPageQuery;
import com.training.common.entity.CourseChapter;
import com.training.mapper.CourseChapterMapper;
import com.training.service.ChapterService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程章节服务实现
 */
@Service
public class ChapterServiceImpl extends ServiceImpl<CourseChapterMapper, CourseChapter> implements ChapterService {

    @Override
    public IPage<CourseChapter> page(ChapterPageQuery query) {
        Page<CourseChapter> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectChapterPage(page, query.getCourseId(), query.getTitle());
    }

    @Override
    public List<CourseChapter> listByCourseId(Long courseId) {
        LambdaQueryWrapper<CourseChapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseChapter::getCourseId, courseId);
        wrapper.orderByAsc(CourseChapter::getSortOrder);
        return list(wrapper);
    }

    @Override
    public boolean create(CourseChapter chapter) {
        if (chapter.getSortOrder() == null) {
            chapter.setSortOrder(0);
        }
        if (chapter.getDuration() == null) {
            chapter.setDuration(0);
        }
        return save(chapter);
    }

    @Override
    public boolean updateChapter(CourseChapter chapter) {
        if (chapter.getId() == null) {
            throw new IllegalArgumentException("章节ID不能为空");
        }
        CourseChapter exist = getById(chapter.getId());
        if (exist == null) {
            throw new IllegalArgumentException("章节不存在");
        }
        return updateById(chapter);
    }

    @Override
    public boolean resort(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return true;
        }
        for (int i = 0; i < idList.size(); i++) {
            CourseChapter chapter = getById(idList.get(i));
            if (chapter == null) {
                throw new IllegalArgumentException("章节不存在：" + idList.get(i));
            }
            chapter.setSortOrder(i + 1);
            updateById(chapter);
        }
        return true;
    }
}
