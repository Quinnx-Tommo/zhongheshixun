package com.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.training.common.dto.TeacherPageQuery;
import com.training.common.entity.Teacher;

/**
 * 讲师服务接口
 */
public interface TeacherService extends IService<Teacher> {

    /**
     * 分页查询讲师列表
     */
    IPage<Teacher> page(TeacherPageQuery query);

    /**
     * 新增讲师
     */
    boolean create(Teacher teacher);

    /**
     * 编辑讲师
     */
    boolean updateTeacher(Teacher teacher);
}
