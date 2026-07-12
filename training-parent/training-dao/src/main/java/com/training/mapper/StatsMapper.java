package com.training.mapper;

import com.training.common.vo.CourseStatVO;
import com.training.common.vo.ExamStatVO;
import com.training.common.vo.MyStatVO;
import com.training.common.vo.OrgStatVO;
import com.training.common.vo.OverviewVO;
import com.training.common.vo.StudentStatVO;
import com.training.common.vo.TrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计 Mapper
 *
 * 说明：统计类查询含多表 JOIN / GROUP BY / 子查询，使用手写 XML 实现。
 */
@Mapper
public interface StatsMapper {

    /** 数据概览 */
    OverviewVO selectOverview();

    /** 今日活跃学员数（按 update_time 当天去重） */
    Long countTodayActiveStudents();

    /** 学员学习统计（分页） */
    List<StudentStatVO> selectStudentStats(@Param("keyword") String keyword,
                                            @Param("orgName") String orgName,
                                            @Param("offset") long offset,
                                            @Param("size") int size);

    /** 学员统计总数（用于分页） */
    Long countStudentStats(@Param("keyword") String keyword,
                           @Param("orgName") String orgName);

    /** 各考试基本统计（平均分/最高最低/通过率） */
    List<ExamStatVO> selectExamStats(@Param("examId") Long examId);

    /** 单场考试分数段分布 */
    List<ExamStatVO.ScoreRangeVO> selectScoreRanges(@Param("examId") Long examId);

    /** 课程热度统计（分页） */
    List<CourseStatVO> selectCourseStats(@Param("keyword") String keyword,
                                          @Param("offset") long offset,
                                          @Param("size") int size);

    /** 课程统计总数 */
    Long countCourseStats(@Param("keyword") String keyword);

    /** 机构维度统计（分页） */
    List<OrgStatVO> selectOrgStats(@Param("offset") long offset,
                                    @Param("size") int size);

    /** 机构统计总数 */
    Long countOrgStats();

    /** 时间趋势（学习时长 + 活跃学员） */
    List<TrendVO.TrendPointVO> selectTrend(@Param("granularity") String granularity,
                                             @Param("fromDate") String fromDate);

    /** 学员个人学习统计 */
    MyStatVO selectMyStat(@Param("studentId") Long studentId);

    /** 学员最近 N 日每日学习时长 */
    List<MyStatVO.DailyHourVO> selectRecentDays(@Param("studentId") Long studentId,
                                                @Param("days") int days);
}
