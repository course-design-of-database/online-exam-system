package org.bishe.question.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.bishe.question.dto.ExamQuestionDTO;
import org.bishe.question.util.SqlUtils;
import org.bishe.question.entity.*;
import org.bishe.question.mapper.*;
import org.bishe.question.service.WrongQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试服务类
 */
@Service
@Transactional
public class ExamService {

    private static final Logger logger = LoggerFactory.getLogger(ExamService.class);

    @Autowired private ExamMapper examMapper;
    @Autowired private ExamQuestionMapper examQuestionMapper;
    @Autowired private ExamRecordMapper examRecordMapper;
    @Autowired private QuestionMapper questionMapper;
    @Autowired private StudentMapper studentMapper;
    @Autowired private StudentAnswerMapper studentAnswerMapper;
    @Autowired private QuestionTypeMapper questionTypeMapper;
    @Autowired private WrongQuestionService wrongQuestionService;

    // Lazy-loaded type cache
    private volatile Map<Long, Question.QuestionType> typeIdToEnum;

    private Question.QuestionType getTypeById(Long typeId) {
        if (typeIdToEnum == null) {
            synchronized (this) {
                if (typeIdToEnum == null) {
                    typeIdToEnum = questionTypeMapper.findAll().stream()
                        .collect(Collectors.toMap(
                            QuestionTypeEntity::getId,
                            e -> {
                                try { return Question.QuestionType.valueOf(e.getName()); }
                                catch (IllegalArgumentException ex) { return null; }
                            }
                        ));
                }
            }
        }
        return typeIdToEnum.get(typeId);
    }

    public Exam createExam(Exam exam) {
        logger.info("=== 开始创建考试 ===");
        logger.info("考试标题: {}", exam.getTitle());
        try {
            validateExamTime(exam);
            if (examMapper.existsByTitle(exam.getTitle()))
                throw new RuntimeException("考试标题已存在");
            examMapper.insert(exam);
            logger.info("考试创建成功，ID: {}", exam.getId());
            return exam;
        } catch (Exception e) {
            logger.error("创建考试失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Exam updateExam(Exam exam) {
        Exam existing = examMapper.findById(exam.getId());
        if (existing == null) throw new RuntimeException("考试不存在");
        if (examMapper.existsByTitleAndIdNot(exam.getTitle(), exam.getId()))
            throw new RuntimeException("考试标题已存在");
        validateExamTimeForUpdate(exam);
        exam.setUpdatedAt(LocalDateTime.now());
        examMapper.update(exam);
        return exam;
    }

    public void deleteExam(Long id) {
        Exam exam = getExamById(id);
        Long recordCount = examRecordMapper.countByExamId(id);
        if (recordCount > 0) throw new RuntimeException("已有学生参加考试，无法删除");
        examQuestionMapper.deleteByExamId(id);
        examMapper.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Exam getExamById(Long id) {
        Exam exam = examMapper.findById(id);
        if (exam == null) throw new RuntimeException("考试不存在");
        return exam;
    }

    @Transactional(readOnly = true)
    public List<Exam> getAllExams() {
        return examMapper.findAll();
    }

    @Transactional(readOnly = true)
    public PageInfo<Exam> getAllExams(int page, int size, String sortBy, String sortDir) {
        logger.info("=== 分页查询所有考试 ===");
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<Exam> list = examMapper.findAll();
        return new PageInfo<>(list);
    }

    @Transactional(readOnly = true)
    public PageInfo<Exam> getExamsByStatus(Integer status, int page, int size, String sortBy, String sortDir) {
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<Exam> list = examMapper.findByStatus(status);
        return new PageInfo<>(list);
    }

    @Transactional(readOnly = true)
    public PageInfo<Exam> getExamsByCreator(Long createdBy, int page, int size, String sortBy, String sortDir) {
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<Exam> list = examMapper.findByCreatedBy(createdBy);
        return new PageInfo<>(list);
    }

    @Transactional(readOnly = true)
    public PageInfo<Exam> searchExamsByTitle(String title, int page, int size, String sortBy, String sortDir) {
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);
        List<Exam> list = examMapper.findByTitleContaining(title);
        return new PageInfo<>(list);
    }

    @Transactional(readOnly = true)
    public List<Exam> getExamsByStatus(Integer status) { return examMapper.findByStatus(status); }

    @Transactional(readOnly = true)
    public List<Exam> getExamsByCreator(Long createdBy) { return examMapper.findByCreatedBy(createdBy); }

    @Transactional(readOnly = true)
    public List<Exam> searchExams(String title) { return examMapper.findByTitleContaining(title); }

    @Transactional(readOnly = true)
    public List<Exam> getActiveExams() { return examMapper.findActiveExams(LocalDateTime.now()); }

    @Transactional(readOnly = true)
    public List<Exam> getUpcomingExams() { return examMapper.findUpcomingExams(LocalDateTime.now()); }

    @Transactional(readOnly = true)
    public List<Exam> getFinishedExams() { return examMapper.findFinishedExams(LocalDateTime.now()); }

    @Transactional(readOnly = true)
    public List<Exam> getAvailableExamsForStudent(Long studentId) {
        logger.info("=== 查询学生可用考试 ===");
        LocalDateTime now = LocalDateTime.now();
        return examMapper.findAvailableExamsForStudent(studentId, now);
    }

    @Transactional(readOnly = true)
    public List<Exam> getExamsByStudent(Long studentId) {
        return examMapper.findExamsByStudent(studentId);
    }

    @Transactional(readOnly = true)
    public List<ExamRecord> getRecentExamRecordsByStudent(Long studentId, int limit) {
        PageHelper.startPage(1, limit, "created_at desc");
        return examRecordMapper.findRecentExamRecordsByStudentId(studentId);
    }

    public void addQuestionToExam(Long examId, Long questionId, Integer order, Double score) {
        if (examMapper.findById(examId) == null) throw new RuntimeException("考试不存在");
        Question q = questionMapper.findById(questionId);
        if (q == null) throw new RuntimeException("题目不存在");
        if (examQuestionMapper.existsByExamIdAndQuestionId(examId, questionId))
            throw new RuntimeException("该题目已经添加到考试中");

        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExamId(examId);
        examQuestion.setQuestionId(questionId);
        if (order != null) examQuestion.setQuestionOrder(order);
        if (score != null) examQuestion.setQuestionScore(BigDecimal.valueOf(score));
        examQuestionMapper.insert(examQuestion);
    }

    public void removeQuestionFromExam(Long examId, Long questionId) {
        Exam exam = getExamById(examId);
        if (exam.getStartTime().isBefore(LocalDateTime.now()))
            throw new RuntimeException("考试已开始，无法移除题目");

        ExamQuestion examQuestion = examQuestionMapper.findByExamIdAndQuestionId(examId, questionId);
        if (examQuestion == null) throw new RuntimeException("题目不在考试中");

        examQuestionMapper.deleteByExamIdAndQuestionId(examId, questionId);
        examQuestionMapper.adjustQuestionOrderAfterDelete(examId, examQuestion.getQuestionOrder());
        updateExamStatistics(examId);
    }

    public void updateQuestionScore(Long examId, Long questionId, Double score) {
        ExamQuestion eq = examQuestionMapper.findByExamIdAndQuestionId(examId, questionId);
        if (eq == null) throw new RuntimeException("考试题目关联不存在");
        if (score == null || score <= 0) throw new RuntimeException("题目分数必须大于0");
        examQuestionMapper.updateQuestionScore(examId, questionId, BigDecimal.valueOf(score));
    }

    @Transactional(readOnly = true)
    public List<ExamQuestionDTO> getExamQuestions(Long examId) {
        if (!examMapper.existsById(examId)) throw new RuntimeException("考试不存在");

        List<ExamQuestion> examQuestions = examQuestionMapper.findByExamIdOrderByQuestionOrder(examId);
        List<ExamQuestionDTO> result = new ArrayList<>();

        for (ExamQuestion examQuestion : examQuestions) {
            ExamQuestionDTO dto = new ExamQuestionDTO();
            dto.setId(examQuestion.getId());
            dto.setExamId(examQuestion.getExamId());
            dto.setQuestionId(examQuestion.getQuestionId());
            dto.setQuestionOrder(examQuestion.getQuestionOrder());
            dto.setScore(examQuestion.getScore());
            dto.setCreatedAt(examQuestion.getCreatedAt());

            if (examQuestion.getQuestionId() != null) {
                Question question = questionMapper.findById(examQuestion.getQuestionId());
                if (question != null) {
                    if (question.getType() == null && question.getTypeId() != null) {
                        question.setType(getTypeById(question.getTypeId()));
                    }
                    dto.setQuestionTitle(question.getTitle());
                    dto.setQuestionContent(question.getContent());
                    dto.setQuestionType(question.getType().name());
                    dto.setQuestionDifficulty(question.getDifficulty().name());
                    dto.setQuestionOptions(question.getOptions());
                    dto.setQuestionCorrectAnswers(question.getCorrectAnswers());
                    dto.setQuestionCorrectAnswer(question.getCorrectAnswer());
                    dto.setQuestionTags(question.getTags());
                }
            }
            result.add(dto);
        }
        return result;
    }

    public void updateExamStatus(Long id, Integer status) {
        Exam exam = examMapper.findById(id);
        if (exam == null) throw new RuntimeException("考试不存在");
        exam.setStatus(status);
        exam.setUpdatedAt(LocalDateTime.now());
        examMapper.update(exam);

        if (status == 2) {
            List<ExamRecord> unsubmitted = examRecordMapper.findByExamIdAndStatus(id, 1);
            LocalDateTime now = LocalDateTime.now();
            for (ExamRecord record : unsubmitted) {
                record.setStatus(2);
                record.setSubmitTime(now);
                if (record.getStartTime() != null) {
                    long minutes = java.time.Duration.between(record.getStartTime(), now).toMinutes();
                    record.setDurationMinutes((int) minutes);
                }
                examRecordMapper.update(record);
            }
        }
    }

    public void updateExpiredExams() {
        examMapper.updateExpiredExamsStatus(LocalDateTime.now(), LocalDateTime.now());
        examRecordMapper.timeoutInProgressRecords(LocalDateTime.now(), LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getExamStatistics(Long examId) {
        Exam exam = examMapper.findById(examId);
        if (exam == null) throw new RuntimeException("考试不存在");

        Long totalParticipants = examRecordMapper.countByExamId(examId);
        Long completedCount = examRecordMapper.countByExamIdAndStatus(examId, 3);
        Long passedCount = examRecordMapper.countByExamIdAndStatusAndTotalScoreGreaterThanEqual(examId, 3, exam.getPassScore());
        Long failedCount = completedCount - passedCount;
        BigDecimal averageScore = examRecordMapper.getAverageScoreByExamIdAndStatus(examId, 3);
        BigDecimal maxScore = examRecordMapper.getMaxScoreByExamIdAndStatus(examId, 3);
        BigDecimal minScore = examRecordMapper.getMinScoreByExamIdAndStatus(examId, 3);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("exam", exam);
        statistics.put("totalParticipants", totalParticipants);
        statistics.put("completedCount", completedCount);
        statistics.put("passedCount", passedCount);
        statistics.put("failedCount", failedCount);
        statistics.put("averageScore", averageScore);
        statistics.put("maxScore", maxScore);
        statistics.put("minScore", minScore);
        statistics.put("completionRate", totalParticipants > 0 ? (double) completedCount / totalParticipants * 100 : 0);
        statistics.put("passRate", completedCount > 0 ? (double) passedCount / completedCount * 100 : 0);
        return statistics;
    }

    @Transactional(readOnly = true)
    public PageInfo<ExamRecord> getAllExamRecords(int page, int size, String sortBy, String sortDir,
                                                   Long examId, String studentName, Integer status) {
        logger.info("=== 分页查询所有考试记录 ===");
        PageHelper.startPage(page + 1, size, SqlUtils.camelToSnake(sortBy) + " " + sortDir);

        List<ExamRecord> records;
        if (examId != null && studentName != null && !studentName.trim().isEmpty() && status != null) {
            records = examRecordMapper.findByExamIdAndStudentNameContainingAndStatus(examId, studentName, status);
        } else if (examId != null && studentName != null && !studentName.trim().isEmpty()) {
            records = examRecordMapper.findByExamIdAndStudentNameContaining(examId, studentName);
        } else if (examId != null && status != null) {
            records = examRecordMapper.findByExamIdAndStatus(examId, status);
        } else if (studentName != null && !studentName.trim().isEmpty() && status != null) {
            records = examRecordMapper.findByStudentNameContainingAndStatus(studentName, status);
        } else if (examId != null) {
            records = examRecordMapper.findByExamId(examId);
        } else if (studentName != null && !studentName.trim().isEmpty()) {
            records = examRecordMapper.findByStudentNameContaining(studentName);
        } else if (status != null) {
            records = examRecordMapper.findByStatus(status);
        } else {
            records = examRecordMapper.findAll();
        }

        return new PageInfo<>(records);
    }

    @Transactional(readOnly = true)
    public List<ExamRecord> getExamRecords(Long examId) {
        return examRecordMapper.findByExamId(examId);
    }

    public void submitExam(Long examId, Long studentId, Map<String, Object> answers, String submitTime) {
        Exam exam = examMapper.findById(examId);
        if (exam == null) throw new RuntimeException("考试不存在");
        if (!exam.isInProgress()) throw new RuntimeException("考试未开始或已结束");

        ExamRecord examRecord = examRecordMapper.findByExamIdAndStudentId(examId, studentId);
        if (examRecord == null) {
            Student student = studentMapper.findById(studentId);
            if (student == null) throw new RuntimeException("学生不存在");
            examRecord = new ExamRecord();
            examRecord.setExamId(examId);
            examRecord.setStudentId(studentId);
            examRecord.setStudentNumber(student.getStudentNumber());
            examRecord.setStudentName(student.getName());
            examRecord.setStartTime(LocalDateTime.now().minusMinutes(exam.getDuration()));
            examRecord.setStatus(1);
        }

        examRecord.setEndTime(LocalDateTime.now());
        examRecord.setSubmitTime(LocalDateTime.now());
        examRecord.setStatus(2);

        if (examRecord.getStartTime() != null) {
            long minutes = java.time.Duration.between(examRecord.getStartTime(), examRecord.getEndTime()).toMinutes();
            examRecord.setDurationMinutes((int) minutes);
        }

        if (examRecord.getId() != null) {
            examRecordMapper.update(examRecord);
        } else {
            examRecordMapper.insert(examRecord);
        }

        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            Long questionId = Long.valueOf(entry.getKey());
            String answer = entry.getValue() != null ? entry.getValue().toString() : "";

            StudentAnswer studentAnswer = studentAnswerMapper
                    .findByExamRecordIdAndQuestionId(examRecord.getId(), questionId);

            if (studentAnswer == null) {
                studentAnswer = new StudentAnswer();
                studentAnswer.setExamRecordId(examRecord.getId());
                studentAnswer.setExamId(examId);
                studentAnswer.setStudentId(studentId);
                studentAnswer.setQuestionId(questionId);
            }

            studentAnswer.setStudentAnswer(answer);
            studentAnswer.setAnswerTime(LocalDateTime.now());

            Question question = questionMapper.findById(questionId);
            if (question != null) {
                if (question.getType() == null && question.getTypeId() != null) {
                    question.setType(getTypeById(question.getTypeId()));
                }
                studentAnswer.setQuestion(question);
                if (question.getType() == Question.QuestionType.single ||
                    question.getType() == Question.QuestionType.multiple) {
                    studentAnswer.setCorrectAnswer(question.getCorrectAnswers());
                } else {
                    studentAnswer.setCorrectAnswer(question.getCorrectAnswer());
                }
                studentAnswer.checkAnswer();

                // Record wrong questions for the wrong-questions book
                if (Boolean.TRUE.equals(studentAnswer.getIsCorrect())) {
                    // Auto-mark as mastered if previously wrong
                    wrongQuestionService.autoMarkMastered(studentId, questionId);
                } else {
                    // Record as wrong
                    wrongQuestionService.recordWrongAnswer(studentId, questionId, examId, answer);
                }
            }

            if (studentAnswer.getId() != null) {
                studentAnswerMapper.update(studentAnswer);
            } else {
                studentAnswerMapper.insert(studentAnswer);
            }
        }

        calculateExamScore(examRecord.getId());
    }

    public void updateExamRecordScore(Long recordId, BigDecimal newScore) {
        ExamRecord examRecord = examRecordMapper.findById(recordId);
        if (examRecord == null) throw new RuntimeException("考试记录不存在");
        if (examRecord.getStatus() != 2 && examRecord.getStatus() != 3)
            throw new RuntimeException("只能编辑已完成的考试记录分数");
        examRecord.setScore(newScore);
        examRecord.setUpdatedAt(LocalDateTime.now());
        examRecordMapper.update(examRecord);
        logger.info("考试记录分数已更新 - 记录ID: {}, 新分数: {}", recordId, newScore);
    }

    public void resetExamRecord(Long recordId) {
        ExamRecord examRecord = examRecordMapper.findById(recordId);
        if (examRecord == null) throw new RuntimeException("考试记录不存在");
        examRecord.setStatus(0);
        examRecord.setStartTime(null);
        examRecord.setEndTime(null);
        examRecord.setSubmitTime(null);
        examRecord.setScore(BigDecimal.ZERO);
        examRecord.setCorrectCount(0);
        examRecord.setWrongCount(0);
        examRecord.setUnansweredCount(0);
        examRecord.setUpdatedAt(LocalDateTime.now());
        studentAnswerMapper.deleteByExamRecordId(recordId);
        examRecordMapper.update(examRecord);
        logger.info("考试记录已重置 - 记录ID: {}", recordId);
    }

    public void deleteExamRecord(Long recordId) {
        ExamRecord examRecord = examRecordMapper.findById(recordId);
        if (examRecord == null) throw new RuntimeException("考试记录不存在");
        studentAnswerMapper.deleteByExamRecordId(recordId);
        examRecordMapper.deleteById(recordId);
        logger.info("考试记录已删除 - 记录ID: {}", recordId);
    }

    private void calculateExamScore(Long examRecordId) {
        ExamRecord examRecord = examRecordMapper.findById(examRecordId);
        if (examRecord == null) throw new RuntimeException("考试记录不存在");

        List<StudentAnswer> studentAnswers = studentAnswerMapper.findByExamRecordId(examRecordId);
        int correctCount = 0, wrongCount = 0, unansweredCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;

        for (StudentAnswer sa : studentAnswers) {
            if (sa.getStudentAnswer() == null || sa.getStudentAnswer().trim().isEmpty()) {
                unansweredCount++;
            } else if (Boolean.TRUE.equals(sa.getIsCorrect())) {
                correctCount++;
                ExamQuestion eq = examQuestionMapper.findByExamIdAndQuestionId(examRecord.getExamId(), sa.getQuestionId());
                if (eq != null && eq.getScore() != null) totalScore = totalScore.add(eq.getScore());
            } else {
                wrongCount++;
            }
        }

        examRecord.setScore(totalScore);
        examRecord.setCorrectCount(correctCount);
        examRecord.setWrongCount(wrongCount);
        examRecord.setUnansweredCount(unansweredCount);
        examRecord.setUpdatedAt(LocalDateTime.now());
        examRecordMapper.update(examRecord);
    }

    private void validateExamTime(Exam exam) {
        if (exam.getStartTime().isAfter(exam.getEndTime()))
            throw new RuntimeException("开始时间不能晚于结束时间");
        LocalDateTime now = LocalDateTime.now();
        if (exam.getStartTime().isBefore(now.minusMinutes(1)))
            throw new RuntimeException("开始时间不能早于当前时间");
        long totalMinutes = java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
        if (totalMinutes < exam.getDuration())
            throw new RuntimeException("考试时长不能超过开始和结束时间的间隔");
    }

    private void validateExamTimeForUpdate(Exam exam) {
        if (exam.getStartTime().isAfter(exam.getEndTime()))
            throw new RuntimeException("开始时间不能晚于结束时间");
        long totalMinutes = java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
        if (totalMinutes < exam.getDuration())
            throw new RuntimeException("考试时长不能超过开始和结束时间的间隔");
    }

    private void updateExamStatistics(Long examId) {
        Long questionCount = examQuestionMapper.countByExamId(examId);
        BigDecimal totalScore = examQuestionMapper.calculateTotalScoreByExamId(examId);
        if (totalScore == null) totalScore = BigDecimal.ZERO;
        Exam exam = getExamById(examId);
        exam.setQuestionCount(questionCount.intValue());
        exam.setTotalScore(totalScore);
        examMapper.update(exam);
    }

    @Transactional(readOnly = true)
    public List<StudentAnswer> getStudentAnswersByExamRecord(Long examRecordId, Long studentId) {
        ExamRecord examRecord = examRecordMapper.findById(examRecordId);
        if (examRecord == null) throw new RuntimeException("考试记录不存在");
        if (!examRecord.getStudentId().equals(studentId)) throw new RuntimeException("无权限查看该考试记录");

        List<StudentAnswer> studentAnswers = studentAnswerMapper.findByExamRecordIdOrderByQuestionId(examRecordId);
        for (StudentAnswer answer : studentAnswers) {
            if (answer.getQuestionId() != null) {
                Question question = questionMapper.findById(answer.getQuestionId());
                if (question != null) {
                    if (question.getType() == null && question.getTypeId() != null) {
                        question.setType(getTypeById(question.getTypeId()));
                    }
                    answer.setQuestion(question);
                }
            }
        }
        return studentAnswers;
    }
}