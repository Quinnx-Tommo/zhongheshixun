package com.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.training.common.dto.QuestionPageQuery;
import com.training.common.entity.Question;

/**
 * 试题服务接口
 */
public interface QuestionService extends IService<Question> {

    /**
     * 分页查询试题列表
     */
    IPage<Question> page(QuestionPageQuery query);

    /**
     * 新增试题
     */
    boolean create(Question question);

    /**
     * 编辑试题
     */
    boolean updateQuestion(Question question);
}
