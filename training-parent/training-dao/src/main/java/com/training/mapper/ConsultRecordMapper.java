package com.training.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.training.common.entity.ConsultRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 咨询记录 Mapper
 */
@Mapper
public interface ConsultRecordMapper extends BaseMapper<ConsultRecord> {

    /**
     * 查询 SLA 超时工单（未回复超过 slaHours 小时）
     * 条件：answer IS NULL AND create_time < (NOW() - slaHours)
     */
    List<ConsultRecord> selectOverdue(@Param("slaHours") int slaHours);

    /**
     * 更新回复内容与回复时间
     */
    int updateReply(@Param("id") Long id,
                    @Param("answer") String answer,
                    @Param("isAuto") int isAuto,
                    @Param("replyTime") LocalDateTime replyTime);
}
