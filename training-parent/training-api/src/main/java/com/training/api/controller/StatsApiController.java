package com.training.api.controller;

import com.training.common.result.Result;
import com.training.common.vo.MyStatVO;
import com.training.service.StatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 小程序 - 学员个人学习统计控制器
 *
 * <p>权限：需登录（由 ApiJwtInterceptor 保障）。
 * userId 统一从 request attribute 取（与 Study/Exam/Consult 保持一致），
 * 不要 @RequestParam，否则前端不传 ?studentId= 会 500。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
public class StatsApiController {

    @Resource
    private StatsService statsService;

    /**
     * 我的学习统计（学员个人）
     *
     * @param request 由 ApiJwtInterceptor 注入 userId attribute
     */
    @GetMapping("/my")
    public Result<MyStatVO> myStat(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            // 兜底：避免 NPE； interceptor 已经 401，这里防御性处理
            return Result.error(401, "未登录");
        }
        return Result.success(statsService.myStat(userId));
    }
}
