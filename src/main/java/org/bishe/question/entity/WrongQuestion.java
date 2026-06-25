package org.bishe.question.entity;

import java.time.LocalDateTime;

/**
 * 错题本实体类
 * 记录学生做错的题目，支持掌握标记和错误次数统计
 */
public class WrongQuestion {

    private Long id;
    private Long studentId;
    private Long questionId;
    private Long examId;
    private Integer wrongCount;
    private Integer mastered;  // 0-未掌握 1-已掌握
    private String lastWrongAnswer;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // Transient fields for joined data
    private String questionTitle;
    private String questionContent;
    private String questionType;
    private String questionDifficulty;
    private String questionOptions;
    private String correctAnswers;
    private String correctAnswer;
    private String questionTags;

    public WrongQuestion() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public Integer getWrongCount() { return wrongCount; }
    public void setWrongCount(Integer wrongCount) { this.wrongCount = wrongCount; }

    public Integer getMastered() { return mastered; }
    public void setMastered(Integer mastered) { this.mastered = mastered; }

    public String getLastWrongAnswer() { return lastWrongAnswer; }
    public void setLastWrongAnswer(String lastWrongAnswer) { this.lastWrongAnswer = lastWrongAnswer; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    public String getQuestionTitle() { return questionTitle; }
    public void setQuestionTitle(String questionTitle) { this.questionTitle = questionTitle; }

    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getQuestionDifficulty() { return questionDifficulty; }
    public void setQuestionDifficulty(String questionDifficulty) { this.questionDifficulty = questionDifficulty; }

    public String getQuestionOptions() { return questionOptions; }
    public void setQuestionOptions(String questionOptions) { this.questionOptions = questionOptions; }

    public String getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(String correctAnswers) { this.correctAnswers = correctAnswers; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getQuestionTags() { return questionTags; }
    public void setQuestionTags(String questionTags) { this.questionTags = questionTags; }
}
