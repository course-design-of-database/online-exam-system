package org.bishe.question.entity;

import java.time.LocalDateTime;

/**
 * 学生答题记录实体类
 */
public class StudentAnswer {

    private Long id;
    private Long examRecordId;
    private Long examId;
    private Long studentId;
    private Long questionId;
    private String studentAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
    private LocalDateTime answerTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联关系（非持久化，仅用于业务逻辑）
    private ExamRecord examRecord;
    private Question question;

    // 构造函数
    public StudentAnswer() {}

    public StudentAnswer(Long examRecordId, Long questionId, String studentAnswer) {
        this.examRecordId = examRecordId;
        this.questionId = questionId;
        this.studentAnswer = studentAnswer;
        this.answerTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getExamRecordId() { return examRecordId; }
    public void setExamRecordId(Long examRecordId) { this.examRecordId = examRecordId; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
        this.answerTime = LocalDateTime.now();
    }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
    public LocalDateTime getAnswerTime() { return answerTime; }
    public void setAnswerTime(LocalDateTime answerTime) { this.answerTime = answerTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public ExamRecord getExamRecord() { return examRecord; }
    public void setExamRecord(ExamRecord examRecord) { this.examRecord = examRecord; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }

    // 业务方法

    /**
     * 检查答案是否正确
     */
    public void checkAnswer() {
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) { this.isCorrect = false; return; }
        if (question != null) { checkAnswerByQuestionType(); } else { checkSimpleAnswer(); }
    }

    private void checkSimpleAnswer() {
        if (correctAnswer == null) { this.isCorrect = false; return; }
        this.isCorrect = studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
    }

    private void checkAnswerByQuestionType() {
        Question.QuestionType type = question.getType();
        switch (type) {
            case single: checkSingleChoiceAnswer(); break;
            case multiple: checkMultipleChoiceAnswer(); break;
            case judge: checkJudgeAnswer(); break;
            case fill: case essay: checkTextAnswer(); break;
            default: this.isCorrect = false;
        }
    }

    private void checkSingleChoiceAnswer() {
        try {
            String correctAnswersJson = question.getCorrectAnswers();
            if (correctAnswersJson == null) { this.isCorrect = false; return; }
            String correct = correctAnswersJson.replaceAll("[\\[\\]\"]", "").trim();
            String studentAns = studentAnswer.trim();
            if (studentAns.matches("\\d+")) {
                int index = Integer.parseInt(studentAns);
                if (index >= 0 && index < 26) studentAns = String.valueOf((char)('A' + index));
            }
            this.isCorrect = correct.equalsIgnoreCase(studentAns);
        } catch (Exception e) { this.isCorrect = false; }
    }

    private void checkMultipleChoiceAnswer() {
        try {
            String correctAnswersJson = question.getCorrectAnswers();
            if (correctAnswersJson == null) { this.isCorrect = false; return; }
            String[] corrects = correctAnswersJson.replaceAll("[\\[\\]\"]", "").split(",");
            String studentAns = studentAnswer.trim();
            if (studentAns.startsWith("[") && studentAns.endsWith("]"))
                studentAns = studentAns.substring(1, studentAns.length() - 1);
            String[] studentArr = studentAns.split(",");
            for (int i = 0; i < studentArr.length; i++) {
                String ans = studentArr[i].trim();
                if (ans.matches("\\d+")) {
                    int index = Integer.parseInt(ans);
                    if (index >= 0 && index < 26) studentArr[i] = String.valueOf((char)('A' + index));
                } else studentArr[i] = ans.toUpperCase();
            }
            if (corrects.length != studentArr.length) { this.isCorrect = false; return; }
            for (String c : corrects) {
                boolean found = false;
                for (String s : studentArr) { if (c.trim().equalsIgnoreCase(s.trim())) { found = true; break; } }
                if (!found) { this.isCorrect = false; return; }
            }
            this.isCorrect = true;
        } catch (Exception e) { this.isCorrect = false; }
    }

    private void checkJudgeAnswer() {
        if (correctAnswer == null) { this.isCorrect = false; return; }
        String studentAns = studentAnswer.trim().toLowerCase();
        String correctAns = correctAnswer.trim().toLowerCase();
        if (correctAns.equals("正确") || correctAns.equals("true") || correctAns.equals("对") || correctAns.equals("t"))
            this.isCorrect = studentAns.equals("true") || studentAns.equals("正确") || studentAns.equals("对") || studentAns.equals("t");
        else if (correctAns.equals("错误") || correctAns.equals("false") || correctAns.equals("错") || correctAns.equals("f"))
            this.isCorrect = studentAns.equals("false") || studentAns.equals("错误") || studentAns.equals("错") || studentAns.equals("f");
        else this.isCorrect = correctAns.equals(studentAns);
    }

    private void checkTextAnswer() {
        if (correctAnswer == null) { this.isCorrect = false; return; }
        String studentAns = studentAnswer.trim().toLowerCase();
        String correctAns = correctAnswer.trim().toLowerCase();
        if (correctAns.contains(",")) {
            for (String c : correctAns.split(",")) { if (studentAns.equals(c.trim())) { this.isCorrect = true; return; } }
            this.isCorrect = false;
        } else this.isCorrect = studentAns.equals(correctAns);
    }

    public boolean isAnswered() { return studentAnswer != null && !studentAnswer.trim().isEmpty(); }

    public void clearAnswer() { this.studentAnswer = null; this.isCorrect = null; this.answerTime = null; }

    @Override
    public String toString() {
        return "StudentAnswer{" + "id=" + id + ", examRecordId=" + examRecordId +
                ", questionId=" + questionId + ", studentAnswer='" + studentAnswer + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' + ", isCorrect=" + isCorrect +
                ", answerTime=" + answerTime + '}';
    }
}