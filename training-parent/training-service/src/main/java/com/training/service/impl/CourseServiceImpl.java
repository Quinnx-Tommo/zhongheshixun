package com.training.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.training.common.dto.CourseDTO;
import com.training.common.dto.CoursePageQuery;
import com.training.common.entity.Course;
import com.training.mapper.CourseMapper;
import com.training.service.CourseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程服务实现
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Override
    public IPage<Course> page(CoursePageQuery query) {
        Page<Course> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectCoursePage(page, query.getTitle(), query.getCourseType(), query.getStatus());
    }

    @Override
    public boolean create(CourseDTO dto) {
        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        // 默认草稿状态
        if (course.getStatus() == null) {
            course.setStatus(0);
        }
        // 默认不支持离线
        if (course.getOfflineFlag() == null) {
            course.setOfflineFlag(0);
        }
        return save(course);
    }

    @Override
    public boolean update(CourseDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        Course course = getById(dto.getId());
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        BeanUtils.copyProperties(dto, course);
        return updateById(course);
    }

    @Override
    public boolean publish(Long id, Integer status) {
        Course course = getById(id);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        course.setStatus(status);
        return updateById(course);
    }

    @Override
    public List<Course> recommend(int limit) {
        // 边界钳制：默认 5，上限 20
        if (limit <= 0) limit = 5;
        if (limit > 20) limit = 20;
        return baseMapper.selectRecommend(limit);
    }
}
