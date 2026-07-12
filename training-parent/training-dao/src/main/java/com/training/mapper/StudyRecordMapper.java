package com.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.training.common.entity.StudyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 学习记录 Mapper
 */
@Mapper
public interface StudyRecordMapper extends BaseMapper<StudyRecord> {

    /**
     *  Upsert 学习记录（累加 study_duration，更新 progress/last_position/completed）
     *  依赖 uk_record(student_id, course_id, chapter_id) 唯一键
     */
    int upsertProgress(@Param("r") StudyRecord record);
}
