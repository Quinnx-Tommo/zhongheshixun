package com.training.mapper;

import com.training.common.entity.ExamRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 考试记录 Mapper
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    /**
     * 统计学员某考试已提交/已批阅的记录数（用于校验重考次数）
     */
    int countFinishedByExamAndStudent(@Param("examId") Long examId,
                                      @Param("studentId") Long studentId);
}
