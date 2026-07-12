package com.training.api.controller;

import com.training.common.dto.ExamSubmitDTO;
import com.training.common.entity.ExamAnswer;
import com.training.common.entity.ExamRecord;
import com.training.common.result.PageResult;
import com.training.common.result.Result;
import com.training.common.vo.ExamListVO;
import com.training.common.vo.ExamResultVO;
import com.training.common.vo.ExamStartVO;
import com.training.mapper.ExamAnswerMapper;
import com.training.mapper.ExamRecordMapper;
import com.training.service.ExamBizService;
import com.training.service.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 小程序考试接口（需要登录）
 */
@Slf4j
@RestController
@RequestMapping("/api/exam")
public class ExamApiController {

    @Resource
    private ExamService examService;

    @Resource
    private ExamBizService examBizService;

    @Resource
    private ExamRecordMapper examRecordMapper;

    @Resource
    private ExamAnswerMapper examAnswerMapper;

    /**
     * 学员维度考试列表（含学员维度状态、剩余重考次数）— P1-7 修复：支持分页
     *
     * @param status   学员维度状态筛选：0未开始 1已完成(已提交) 2已批阅；null/不传表示全部
     * @param pageNum  页码（1-based），默认 1
     * @param pageSize 每页条数，默认 9
     * @param userId   当前学员ID（由拦截器注入）
     */
    @GetMapping("/list")
    public Result<PageResult<ExamListVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "9") Integer pageSize,
            @RequestAttribute("userId") Long userId) {
        return Result.success(examBizService.listForStudent(userId, status, pageNum, pageSize));
    }

    /**
     * 开始考试
     */
    @PostMapping("/start/{id}")
    public Result<ExamStartVO> start(@PathVariable Long id,
                                     @RequestAttribute("userId") Long userId) {
        ExamStartVO vo = examBizService.startExam(id, userId);
        return Result.success(vo);
    }

    /**
     * 提交考试（自动阅卷）
     */
    @PostMapping("/submit")
    public Result<ExamResultVO> submit(@RequestBody @Valid ExamSubmitDTO dto,
                                       @RequestAttribute("userId") Long userId) {
        ExamResultVO vo = examBizService.submitExam(dto.getExamId(), userId, dto.getAnswers());
        return Result.success(vo);
    }

    /**
     * 考试记录详情
     */
    @GetMapping("/record/{id}")
    public Result<ExamRecord> recordDetail(@PathVariable Long id) {
        ExamRecord record = examRecordMapper.selectById(id);
        if (record == null) {
            return Result.error(404, "考试记录不存在");
        }
        return Result.success(record);
    }

    /**
     * 我的考试记录列表
     */
    @GetMapping("/my-records")
    public Result<List<ExamRecord>> myRecords(@RequestAttribute("userId") Long userId) {
        List<ExamRecord> list = examRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, userId)
                        .orderByDesc(ExamRecord::getCreateTime)
        );
        return Result.success(list);
    }

    /**
     * 查看成绩（按 examId 查最新一条已批阅记录 + 聚合答题详情）
     * 用于"查看成绩"按钮跳转（无 query 参数场景）
     */
    @GetMapping("/result")
    public Result<ExamResultVO> result(@RequestParam Long examId,
                                       @RequestAttribute("userId") Long userId) {
        ExamRecord record = examRecordMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, userId)
                        .eq(ExamRecord::getExamId, examId)
                        .orderByDesc(ExamRecord::getCreateTime)
                        .last("LIMIT 1")
        );
        if (record == null) {
            return Result.error(404, "考试记录不存在");
        }
        // 聚合答题详情
        List<ExamAnswer> answers = examAnswerMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamAnswer>()
                        .eq(ExamAnswer::getRecordId, record.getId())
        );
        long correctCount = answers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 1).count();
        long wrongCount = answers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 0).count();
        long unansweredCount = answers.stream().filter(a -> a.getStudentAnswer() == null || a.getStudentAnswer().trim().isEmpty()).count();

        ExamResultVO vo = new ExamResultVO();
        vo.setScore(record.getScore());
        vo.setTotalScore(record.getScore() != null ? record.getScore().intValue() : 0); // 简化：总分从 record 取
        vo.setPassed(record.getScore() != null && record.getScore() >= 60); // 简化判定
        vo.setCorrectCount((int) correctCount);
        vo.setWrongCount((int) wrongCount);
        vo.setUnansweredCount((int) unansweredCount);
        return Result.success(vo);
    }
}
