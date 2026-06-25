package org.bishe.question.controller;

import com.github.pagehelper.PageInfo;
import org.bishe.question.dto.ApiResponse;
import org.bishe.question.dto.ExamQuestionDTO;
import org.bishe.question.entity.Exam;
import org.bishe.question.entity.ExamRecord;
import org.bishe.question.service.ExamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/exams")
@CrossOrigin(origins = "*")
public class ExamController {

    private static final Logger logger = LoggerFactory.getLogger(ExamController.class);

    @Autowired
    private ExamService examService;

    @GetMapping
    public ApiResponse<PageInfo<Exam>> getExams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long createdBy) {
        logger.info("获取考试列表请求 - page: {}, size: {}, sortBy: {}, sortDir: {}, title: {}, status: {}, createdBy: {}",
                   page, size, sortBy, sortDir, title, status, createdBy);
        try {
            PageInfo<Exam> exams;
            if (title != null && !title.trim().isEmpty()) {
                exams = examService.searchExamsByTitle(title, page, size, sortBy, sortDir);
            } else if (status != null) {
                exams = examService.getExamsByStatus(status, page, size, sortBy, sortDir);
            } else if (createdBy != null) {
                exams = examService.getExamsByCreator(createdBy, page, size, sortBy, sortDir);
            } else {
                exams = examService.getAllExams(page, size, sortBy, sortDir);
            }
            return ApiResponse.success("获取考试列表成功", exams);
        } catch (Exception e) {
            logger.error("获取考试列表失败", e);
            return ApiResponse.error("获取考试列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<Exam> getExamById(@PathVariable Long id) {
        try {
            Exam exam = examService.getExamById(id);
            return ApiResponse.success("获取考试详情成功", exam);
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取考试详情失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ApiResponse<Exam> createExam(@Valid @RequestBody Exam exam) {
        try {
            Exam createdExam = examService.createExam(exam);
            return ApiResponse.success("创建考试成功", createdExam);
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("创建考试失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Exam> updateExam(@PathVariable Long id, @Valid @RequestBody Exam exam) {
        try {
            exam.setId(id);
            Exam updatedExam = examService.updateExam(exam);
            return ApiResponse.success("更新考试成功", updatedExam);
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("更新考试失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteExam(@PathVariable Long id) {
        try {
            examService.deleteExam(id);
            return ApiResponse.success("删除考试成功");
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("删除考试失败: " + e.getMessage());
        }
    }

    @GetMapping("/{examId}/questions")
    public ApiResponse<List<ExamQuestionDTO>> getExamQuestions(@PathVariable Long examId) {
        try {
            List<ExamQuestionDTO> questions = examService.getExamQuestions(examId);
            return ApiResponse.success("获取考试题目成功", questions);
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取考试题目失败: " + e.getMessage());
        }
    }

    @PostMapping("/{examId}/questions")
    public ApiResponse<String> addQuestionToExam(@PathVariable Long examId, @RequestBody Map<String, Object> request) {
        try {
            Long questionId = Long.valueOf(request.get("questionId").toString());
            Integer order = request.get("order") != null ? Integer.valueOf(request.get("order").toString()) : null;
            Double score = request.get("score") != null ? Double.valueOf(request.get("score").toString()) : null;
            examService.addQuestionToExam(examId, questionId, order, score);
            return ApiResponse.success("添加题目成功");
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("添加题目失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{examId}/questions/{questionId}")
    public ApiResponse<String> removeQuestionFromExam(@PathVariable Long examId, @PathVariable Long questionId) {
        try {
            examService.removeQuestionFromExam(examId, questionId);
            return ApiResponse.success("移除题目成功");
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("移除题目失败: " + e.getMessage());
        }
    }

    @PutMapping("/{examId}/questions/{questionId}/score")
    public ApiResponse<String> updateQuestionScore(@PathVariable Long examId, @PathVariable Long questionId,
                                                    @RequestBody Map<String, Double> request) {
        try {
            examService.updateQuestionScore(examId, questionId, request.get("score"));
            return ApiResponse.success("更新题目分数成功");
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("更新题目分数失败: " + e.getMessage());
        }
    }

    @GetMapping("/{examId}/statistics")
    public ApiResponse<Map<String, Object>> getExamStatistics(@PathVariable Long examId) {
        try {
            return ApiResponse.success("获取考试统计成功", examService.getExamStatistics(examId));
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取考试统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/records")
    public ApiResponse<PageInfo<ExamRecord>> getAllExamRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) Integer status) {
        try {
            PageInfo<ExamRecord> records = examService.getAllExamRecords(page, size, sortBy, sortDir, examId, studentName, status);
            return ApiResponse.success("获取考试记录列表成功", records);
        } catch (Exception e) {
            logger.error("获取考试记录列表失败", e);
            return ApiResponse.error("获取考试记录列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{examId}/records")
    public ApiResponse<List<ExamRecord>> getExamRecords(@PathVariable Long examId) {
        try {
            return ApiResponse.success("获取考试记录成功", examService.getExamRecords(examId));
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取考试记录失败: " + e.getMessage());
        }
    }

    @GetMapping("/active")
    public ApiResponse<List<Exam>> getActiveExams() {
        try {
            return ApiResponse.success("获取正在进行的考试成功", examService.getActiveExams());
        } catch (Exception e) {
            return ApiResponse.error("获取正在进行的考试失败: " + e.getMessage());
        }
    }

    @GetMapping("/upcoming")
    public ApiResponse<List<Exam>> getUpcomingExams() {
        try {
            return ApiResponse.success("获取即将开始的考试成功", examService.getUpcomingExams());
        } catch (Exception e) {
            return ApiResponse.error("获取即将开始的考试失败: " + e.getMessage());
        }
    }

    @PutMapping("/records/{recordId}/score")
    public ApiResponse<String> updateExamRecordScore(@PathVariable Long recordId, @RequestBody Map<String, Object> request) {
        try {
            Object scoreObj = request.get("score");
            if (scoreObj == null) return ApiResponse.badRequest("分数不能为空");
            BigDecimal newScore = scoreObj instanceof Number ?
                BigDecimal.valueOf(((Number) scoreObj).doubleValue()) : new BigDecimal(scoreObj.toString());
            examService.updateExamRecordScore(recordId, newScore);
            return ApiResponse.success("编辑考试记录分数成功");
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("编辑考试记录分数失败: " + e.getMessage());
        }
    }

    @PostMapping("/records/{recordId}/reset")
    public ApiResponse<String> resetExamRecord(@PathVariable Long recordId) {
        try {
            examService.resetExamRecord(recordId);
            return ApiResponse.success("重新考试设置成功");
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("重新考试失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/records/{recordId}")
    public ApiResponse<String> deleteExamRecord(@PathVariable Long recordId) {
        try {
            examService.deleteExamRecord(recordId);
            return ApiResponse.success("删除考试记录成功");
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("删除考试记录失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ApiResponse<String> updateExamStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            examService.updateExamStatus(id, request.get("status"));
            return ApiResponse.success("更新考试状态成功");
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("更新考试状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/submit")
    public ApiResponse<String> submitExam(@RequestBody Map<String, Object> request) {
        try {
            Long examId = Long.valueOf(request.get("examId").toString());
            Long studentId = Long.valueOf(request.get("studentId").toString());
            @SuppressWarnings("unchecked")
            Map<String, Object> answers = (Map<String, Object>) request.get("answers");
            String submitTime = request.get("submitTime").toString();
            examService.submitExam(examId, studentId, answers, submitTime);
            return ApiResponse.success("考试提交成功");
        } catch (RuntimeException e) {
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("考试提交失败: " + e.getMessage());
        }
    }
}