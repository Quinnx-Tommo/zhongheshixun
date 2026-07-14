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
     * 查询 SLA 超时工单（未回复 且 create_time 距今 >= slaMinutes 分钟）
     */
    List<ConsultRecord> selectOverdue(@Param("slaMinutes") int slaMinutes);

    /**
     * 批量标记超时：把未回复且超时（create_time 距今 >= slaMinutes 分钟）的工单 sla_exceeded 置 1
     *
     * @return 受影响行数（本次新标记为超时的工单数）
     */
    int markSlaExceeded(@Param("slaMinutes") int slaMinutes);

    /**
     * 更新回复内容与回复时间（同时清除 SLA 超时标记）
     */
    int updateReply(@Param("id") Long id,
                    @Param("answer") String answer,
                    @Param("isAuto") int isAuto,
                    @Param("replyTime") LocalDateTime replyTime);
}
