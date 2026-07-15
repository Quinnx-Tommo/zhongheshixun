package com.training.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.training.common.vo.CourseStatVO;
import com.training.common.vo.ExamStatVO;
import com.training.common.vo.MyStatVO;
import com.training.common.vo.OrgStatVO;
import com.training.common.vo.OverviewVO;
import com.training.common.vo.PlatformStatVO;
import com.training.common.vo.StudentStatVO;
import com.training.common.vo.TrendVO;
import com.training.mapper.StatsMapper;
import com.training.service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 统计服务实现
 *
 * 说明：
 * - 统计类查询含多表 JOIN / GROUP BY，使用 StatsMapper 手写 XML。
 * - 时间趋势的 fromDate 由 granularity + recentDays 推导。
 */
@Slf4j
@Service
public class StatsServiceImpl implements StatsService {

    @Resource
    private StatsMapper statsMapper;

    @Override
    public OverviewVO overview() {
        OverviewVO vo = statsMapper.selectOverview();
        if (vo == null) {
            vo = new OverviewVO();
        }
        // 学习时长由秒转小时
        if (vo.getTotalStudySeconds() != null) {
            vo.setTotalStudyHours(Math.round(vo.getTotalStudySeconds() / 3600.0 * 100.0) / 100.0);
        }
        vo.setTodayActiveStudents(statsMapper.countTodayActiveStudents());
        return vo;
    }

    @Override
    public IPage<StudentStatVO> studentStats(String keyword, String orgName, int pageNum, int pageSize) {
        Page<StudentStatVO> page = new Page<>(pageNum, pageSize);
        long offset = (long) (pageNum - 1) * pageSize;
        Long total = statsMapper.countStudentStats(keyword, orgName);
        page.setTotal(total == null ? 0 : total);
        if (total != null && total > 0) {
            page.setRecords(statsMapper.selectStudentStats(keyword, orgName, offset, pageSize));
        } else {
            page.setRecords(new ArrayList<>());
        }
        return page;
    }

    @Override
    public List<ExamStatVO> examStats(Long examId) {
        List<ExamStatVO> list = statsMapper.selectExamStats(examId);
        if (list == null) {
            return new ArrayList<>();
        }
        // 为每场考试填充分数段分布
        for (ExamStatVO vo : list) {
            List<ExamStatVO.ScoreRangeVO> ranges = statsMapper.selectScoreRanges(vo.getExamId());
            vo.setScoreRanges(ranges == null ? new ArrayList<>() : ranges);
        }
        return list;
    }

    @Override
    public IPage<CourseStatVO> courseStats(String keyword, int pageNum, int pageSize) {
        Page<CourseStatVO> page = new Page<>(pageNum, pageSize);
        long offset = (long) (pageNum - 1) * pageSize;
        Long total = statsMapper.countCourseStats(keyword);
        page.setTotal(total == null ? 0 : total);
        if (total != null && total > 0) {
            page.setRecords(statsMapper.selectCourseStats(keyword, offset, pageSize));
        } else {
            page.setRecords(new ArrayList<>());
        }
        return page;
    }

    @Override
    public IPage<OrgStatVO> orgStats(int pageNum, int pageSize) {
        Page<OrgStatVO> page = new Page<>(pageNum, pageSize);
        long offset = (long) (pageNum - 1) * pageSize;
        Long total = statsMapper.countOrgStats();
        page.setTotal(total == null ? 0 : total);
        if (total != null && total > 0) {
            page.setRecords(statsMapper.selectOrgStats(offset, pageSize));
        } else {
            page.setRecords(new ArrayList<>());
        }
        return page;
    }

    @Override
    public TrendVO trend(String granularity, Integer recentDays) {
        if (!StringUtils.hasText(granularity)) {
            granularity = "day";
        }
        if (recentDays == null || recentDays <= 0) {
            recentDays = 30;
        }
        // 推导起始日期
        LocalDate fromDate = LocalDate.now().minusDays(
                "week".equals(granularity) ? recentDays * 7L :
                        "month".equals(granularity) ? recentDays * 30L : recentDays);
        List<TrendVO.TrendPointVO> points = statsMapper.selectTrend(granularity, fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

        TrendVO vo = new TrendVO();
        vo.setGranularity(granularity);
        vo.setPoints(points == null ? new ArrayList<>() : points);
        return vo;
    }

    @Override
    public MyStatVO myStat(Long studentId) {
        MyStatVO vo = statsMapper.selectMyStat(studentId);
        if (vo == null) {
            vo = new MyStatVO();
            vo.setStudentId(studentId);
        }
        // 最近 7 天每日学习时长
        List<MyStatVO.DailyHourVO> raw = statsMapper.selectRecentDays(studentId, 7);
        // 补全缺失日期为 0（保证前端 7 个数据点都存在，趋势图不出现"示例数据"标签）
        vo.setRecent7Days(fillRecentDays(raw, 7));
        return vo;
    }

    @Override
    public PlatformStatVO platform() {
        PlatformStatVO vo = statsMapper.selectPlatform();
        if (vo == null) {
            vo = new PlatformStatVO();
        }
        // COUNT 查询可能返回 null，统一兜底为 0
        if (vo.getOnlineCount() == null) vo.setOnlineCount(0);
        if (vo.getTodayStudyCount() == null) vo.setTodayStudyCount(0);
        if (vo.getConcurrentExamCount() == null) vo.setConcurrentExamCount(0);
        return vo;
    }

    /**
     * 把按日期分组的 SQL 结果补全为最近 N 天的连续数组。
     * - 缺失日期补 0（hours=0.0, minutes=0）
     * - 数据库里超出窗口的旧记录被丢弃
     * - 已存在的日期保持 SQL 真实聚合
     *
     * @param raw  SQL 聚合的原始结果（可能 < N）
     * @param days 窗口大小（默认 7）
     * @return 长度恰为 days 的数组，按日期升序
     */
    private List<MyStatVO.DailyHourVO> fillRecentDays(List<MyStatVO.DailyHourVO> raw, int days) {
        if (raw == null) {
            raw = new ArrayList<>();
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        // 构造"日期 -> 已聚合值"索引
        java.util.Map<String, MyStatVO.DailyHourVO> index = new java.util.HashMap<>(raw.size() * 2);
        for (MyStatVO.DailyHourVO v : raw) {
            if (v.getDate() != null) {
                index.put(v.getDate(), v);
            }
        }
        List<MyStatVO.DailyHourVO> out = new ArrayList<>(days);
        for (int i = days - 1; i >= 0; i--) {
            java.time.LocalDate d = today.minusDays(i);
            String key = d.format(DateTimeFormatter.ISO_LOCAL_DATE);
            MyStatVO.DailyHourVO existed = index.get(key);
            if (existed != null) {
                out.add(existed);
            } else {
                MyStatVO.DailyHourVO empty = new MyStatVO.DailyHourVO();
                empty.setDate(key);
                empty.setHours(0.0);
                empty.setMinutes(0);
                out.add(empty);
            }
        }
        return out;
    }
}
