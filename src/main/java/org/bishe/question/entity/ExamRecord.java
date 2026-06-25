package org.bishe.question.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试记录实体类
 */
public class ExamRecord {

    private Long id;
    private Long examId;
    private Long studentId;
    private String studentNumber;
    private String studentName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime submitTime;
    private BigDecimal totalScore;
    private BigDecimal score;
    private Integer correctCount = 0;
    private Integer wrongCount = 0;
    private Integer unansweredCount = 0;
    private Integer status = 0; // 0-未开始, 1-进行中, 2-已提交, 3-超时提交
    private Integer durationMinutes;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联关系（非持久化，仅用于业务逻辑）
    private Exam exam;
    private Student student;
    private List<StudentAnswer> studentAnswers;

    // 构造函数
    public ExamRecord() {}

    public ExamRecord(Long examId, Long studentId) {
        this.examId = examId;
        this.studentId = studentId;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public Integer getCorrectCount() { return correctCount != null ? correctCount : 0; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }
    public Integer getWrongCount() { return wrongCount != null ? wrongCount : 0; }
    public void setWrongCount(Integer wrongCount) { this.wrongCount = wrongCount; }
    public Integer getUnansweredCount() { return unansweredCount != null ? unansweredCount : 0; }
    public void setUnansweredCount(Integer unansweredCount) { this.unansweredCount = unansweredCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public List<StudentAnswer> getStudentAnswers() { return studentAnswers; }
    public void setStudentAnswers(List<StudentAnswer> studentAnswers) { this.studentAnswers = studentAnswers; }

    // 业务方法
    public void startExam() { this.startTime = LocalDateTime.now(); this.status = 1; }

    public void submitExam() {
        this.submitTime = LocalDateTime.now();
        this.endTime = this.submitTime;
        this.status = 2;
        if (this.startTime != null)
            this.durationMinutes = (int) java.time.Duration.between(this.startTime, this.submitTime).toMinutes();
    }

    public void timeoutSubmit() {
        this.submitTime = LocalDateTime.now();
        this.endTime = this.submitTime;
        this.status = 3;
        if (this.startTime != null)
            this.durationMinutes = (int) java.time.Duration.between(this.startTime, this.submitTime).toMinutes();
    }

    public boolean isCompleted() { return status == 2 || status == 3; }
    public boolean isInProgress() { return status == 1; }

    public double getAccuracy() {
        int correct = correctCount != null ? correctCount : 0;
        int wrong = wrongCount != null ? wrongCount : 0;
        int total = correct + wrong;
        return total == 0 ? 0.0 : (double) correct / total * 100;
    }

    public double getScoreRate() {
        if (totalScore == null || totalScore.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        if (score == null) return 0.0;
        return score.divide(totalScore, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    @Override
    public String toString() {
        return "ExamRecord{" + "id=" + id + ", examId=" + examId + ", studentId=" + studentId +
                ", startTime=" + startTime + ", submitTime=" + submitTime +
                ", score=" + score + ", totalScore=" + totalScore + ", status=" + status +
                ", correctCount=" + correctCount + ", wrongCount=" + wrongCount +
                ", unansweredCount=" + unansweredCount + '}';
    }
}