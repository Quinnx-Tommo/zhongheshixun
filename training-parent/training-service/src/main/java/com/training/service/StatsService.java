package com.training.service;

import com.training.common.vo.CourseStatVO;
import com.training.common.vo.ExamStatVO;
import com.training.common.vo.MyStatVO;
import com.training.common.vo.OrgStatVO;
import com.training.common.vo.OverviewVO;
import com.training.common.vo.PlatformStatVO;
import com.training.common.vo.StudentStatVO;
import com.training.common.vo.TrendVO;

import java.util.List;

/**
 * 统计服务接口
 */
public interface StatsService {

    /** 数据概览 */
    OverviewVO overview();

    /** 学员学习统计（分页） */
    com.baomidou.mybatisplus.core.metadata.IPage<StudentStatVO> studentStats(String keyword, String orgName,
                                                                           int pageNum, int pageSize);

    /** 考试统计（含分数段分布） */
    List<ExamStatVO> examStats(Long examId);

    /** 课程热度统计（分页） */
    com.baomidou.mybatisplus.core.metadata.IPage<CourseStatVO> courseStats(String keyword,
                                                                           int pageNum, int pageSize);

    /** 机构维度统计（分页） */
    com.baomidou.mybatisplus.core.metadata.IPage<OrgStatVO> orgStats(int pageNum, int pageSize);

    /** 时间趋势 */
    TrendVO trend(String granularity, Integer recentDays);

    /** 学员个人学习统计（小程序） */
    MyStatVO myStat(Long studentId);

    /** 平台运行情况（在线人数/今日活跃/并发考试数） */
    PlatformStatVO platform();
}
