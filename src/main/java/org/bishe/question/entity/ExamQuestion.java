package org.bishe.question.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试题目关联实体类
 */
public class ExamQuestion {

    private Long id;
    private Long examId;
    private Long questionId;
    private Integer questionOrder;
    private BigDecimal score;
    private LocalDateTime createdAt;

    // 关联关系（非持久化，仅用于业务逻辑）
    private Exam exam;
    private Question question;

    // 构造函数
    public ExamQuestion() {}

    public ExamQuestion(Long examId, Long questionId, Integer questionOrder, BigDecimal score) {
        this.examId = examId;
        this.questionId = questionId;
        this.questionOrder = questionOrder;
        this.score = score;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Integer getQuestionOrder() { return questionOrder; }
    public void setQuestionOrder(Integer questionOrder) { this.questionOrder = questionOrder; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public void setQuestionScore(BigDecimal score) { this.score = score; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    @Override
    public String toString() {
        return "ExamQuestion{" + "id=" + id + ", examId=" + examId +
                ", questionId=" + questionId + ", questionOrder=" + questionOrder +
                ", score=" + score + ", createdAt=" + createdAt + '}';
    }
}