package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.ExamGenerateDTO;
import com.training.common.dto.ExamPageQuery;
import com.training.common.dto.QuestionPageQuery;
import com.training.common.entity.Exam;
import com.training.common.entity.Question;
import com.training.common.result.Result;
import com.training.service.ExamBizService;
import com.training.service.ExamService;
import com.training.service.QuestionService;
import com.training.common.vo.GenerateResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 考试管理控制器（后台管理端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/exam")
public class ExamController {

    @Resource
    private ExamService examService;

    @Resource
    private ExamBizService examBizService;

    /**
     * 分页列表
     */
    @PreAuthorize("hasAuthority('exam:read')")
    @GetMapping("/page")
    public Result<IPage<Exam>> page(ExamPageQuery query) {
        IPage<Exam> result = examService.page(query);
        return Result.success(result);
    }

    /**
     * 新增考试
     */
    @PreAuthorize("hasAuthority('exam:write')")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid Exam exam) {
        boolean ok = examService.create(exam);
        return Result.success(ok);
    }

    /**
     * 编辑考试
     */
    @PreAuthorize("hasAuthority('exam:write')")
    @PutMapping
    public Result<Boolean> update(@RequestBody @Valid Exam exam) {
        boolean ok = examService.updateExam(exam);
        return Result.success(ok);
    }

    /**
     * 删除考试（逻辑删除）
     */
    @PreAuthorize("hasAuthority('exam:write')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = examService.removeById(id);
        return Result.success(ok);
    }

    /**
     * 考试详情
     */
    @PreAuthorize("hasAuthority('exam:read')")
    @GetMapping("/{id}")
    public Result<Exam> detail(@PathVariable Long id) {
        Exam exam = examService.getById(id);
        if (exam == null) {
            return Result.error(404, "考试不存在");
        }
        return Result.success(exam);
    }

    /**
     * 自动组卷（按知识点抽题）
     * 管理员生成模板试卷：持久化到 exam_paper（studentId=0L 标识模板），并返回题目ID列表
     */
    @PreAuthorize("hasAuthority('exam:write')")
    @PostMapping("/generate")
    public Result<GenerateResultVO> generate(@RequestBody @Valid ExamGenerateDTO dto) {
        // TODO: adminId 可从 SecurityContext 获取，这里暂用 0L 占位
        Long adminId = 0L;
        GenerateResultVO vo = examBizService.generateAndSavePaper(
                dto.getExamId(), dto.getKnowledgePointIds(), adminId);
        return Result.success(vo);
    }

    /**
     * P1-5 修复：发布考试（status 0 草稿 → 1 已发布）
     *
     * <p>对应当前管理后台操作列"发布"按钮（演示阶段未引入管理前端时，
     * 教师 / DBA 可通过 curl 触发：POST /admin/exam/publish/{id}）。</p>
     */
    @PreAuthorize("hasAuthority('exam:write')")
    @PostMapping("/publish/{id}")
    public Result<Boolean> publish(@PathVariable Long id) {
        boolean ok = examService.publish(id);
        return Result.success(ok);
    }

    /**
     * P1-5 修复：下架考试（status 1 已发布 → 2 已下架）
     */
    @PreAuthorize("hasAuthority('exam:write')")
    @PostMapping("/offline/{id}")
    public Result<Boolean> offline(@PathVariable Long id) {
        boolean ok = examService.offline(id);
        return Result.success(ok);
    }
}
