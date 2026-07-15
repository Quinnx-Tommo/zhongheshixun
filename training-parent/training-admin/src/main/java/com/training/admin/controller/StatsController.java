package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.result.PageResult;
import com.training.common.result.Result;
import com.training.common.vo.CourseStatVO;
import com.training.common.vo.ExamStatVO;
import com.training.common.vo.OrgStatVO;
import com.training.common.vo.OverviewVO;
import com.training.common.vo.PlatformStatVO;
import com.training.common.vo.StudentStatVO;
import com.training.common.vo.TrendVO;
import com.training.service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 统计报表控制器（后台管理端）
 *
 * 权限：需 ADMIN role（由 JwtInterceptor + SecurityConfig 保障）
 */
@Slf4j
@RestController
@RequestMapping("/admin/stats")
public class StatsController {

    @Resource
    private StatsService statsService;

    /**
     * 数据概览
     */
    @PreAuthorize("hasAuthority('stats:read')")
    @GetMapping("/overview")
    public Result<OverviewVO> overview() {
        return Result.success(statsService.overview());
    }

    /**
     * 学员学习统计（微观、分页）
     */
    @PreAuthorize("hasAuthority('stats:read')")
    @GetMapping("/student")
    public Result<PageResult<StudentStatVO>> studentStats(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orgName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<StudentStatVO> page = statsService.studentStats(keyword, orgName, pageNum, pageSize);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    /**
     * 考试统计（含分数段分布）
     */
    @PreAuthorize("hasAuthority('stats:read')")
    @GetMapping("/exam")
    public Result<List<ExamStatVO>> examStats(
            @RequestParam(required = false) Long examId) {
        return Result.success(statsService.examStats(examId));
    }

    /**
     * 课程热度统计（宏观、分页）
     */
    @PreAuthorize("hasAuthority('stats:read')")
    @GetMapping("/course")
    public Result<PageResult<CourseStatVO>> courseStats(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<CourseStatVO> page = statsService.courseStats(keyword, pageNum, pageSize);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    /**
     * 机构维度统计（分页）
     */
    @PreAuthorize("hasAuthority('stats:read')")
    @GetMapping("/org")
    public Result<PageResult<OrgStatVO>> orgStats(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        IPage<OrgStatVO> page = statsService.orgStats(pageNum, pageSize);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    /**
     * 时间趋势统计
     *
     * @param granularity day / week / month
     * @param recentDays  近 N 天（默认 30）
     */
    @PreAuthorize("hasAuthority('stats:read')")
    @GetMapping("/trend")
    public Result<TrendVO> trend(
            @RequestParam(defaultValue = "day") String granularity,
            @RequestParam(defaultValue = "30") Integer recentDays) {
        return Result.success(statsService.trend(granularity, recentDays));
    }

    /**
     * 平台运行情况（在线人数/今日活跃/并发考试数）
     * <p>对应 docx 高并发/平台运行监控亮点。</p>
     */
    @PreAuthorize("hasAuthority('stats:read')")
    @GetMapping("/platform")
    public Result<PlatformStatVO> platform() {
        PlatformStatVO vo = statsService.platform();
        return Result.success(vo == null ? new PlatformStatVO() : vo);
    }
}
