package com.training.mapper;

import com.training.common.entity.ExamAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题记录 Mapper
 */
@Mapper
public interface ExamAnswerMapper extends BaseMapper<ExamAnswer> {
}
