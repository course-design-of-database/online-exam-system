package org.bishe.question.entity;

import java.time.LocalDateTime;

/**
 * 题目实体类
 */
public class Question {

    private Long id;
    private String title;
    private String content;
    private Long typeId;
    private QuestionType type;
    private Difficulty difficulty;
    private String options;
    private String correctAnswers;
    private String correctAnswer;
    private String tags;
    private Integer status = 1;
    private Long createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // 题目类型枚举
    public enum QuestionType {
        single,   // 单选题
        multiple, // 多选题
        judge,    // 判断题
        fill,     // 填空题
        essay     // 问答题
    }

    // 难度等级枚举
    public enum Difficulty {
        easy,     // 简单
        medium,   // 中等
        hard      // 困难
    }

    // 构造函数
    public Question() {}

    public Question(String title, String content, QuestionType type, Difficulty difficulty) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.difficulty = difficulty;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(String correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", difficulty=" + difficulty +
                ", status=" + status +
                ", createdTime=" + createdTime +
                '}';
    }
}