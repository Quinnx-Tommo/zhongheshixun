package com.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.training.common.dto.ExamPageQuery;
import com.training.common.entity.Exam;

/**
 * 考试服务接口
 */
public interface ExamService extends IService<Exam> {

    /**
     * 分页查询考试列表
     */
    IPage<Exam> page(ExamPageQuery query);

    /**
     * 新增考试
     */
    boolean create(Exam exam);

    /**
     * 编辑考试
     */
    boolean updateExam(Exam exam);

    /**
     * P1-5 修复：发布考试（status: 0 草稿 -> 1 已发布）
     *
     * <p>管理员操作列"发布/上下架"按钮对应后端能力。发布前会做基本校验：
     * 试卷必须存在、题量 > 0，否则抛业务异常。</p>
     *
     * @param examId 考试ID
     * @return 是否成功
     */
    boolean publish(Long examId);

    /**
     * P1-5 修复：下架考试（status: 1 已发布 -> 2 已下架）
     *
     * @param examId 考试ID
     * @return 是否成功
     */
    boolean offline(Long examId);
}
