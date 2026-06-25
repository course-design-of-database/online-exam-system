package org.bishe.question.dto;

/**
 * 错题本响应DTO
 */
public class WrongQuestionDTO {

    private Long id;
    private Long questionId;
    private String questionTitle;
    private String questionContent;
    private String questionType;
    private String questionDifficulty;
    private String questionOptions;
    private String correctAnswers;
    private String correctAnswer;
    private String questionTags;
    private Integer wrongCount;
    private Integer mastered;
    private String lastWrongAnswer;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }

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

    public Integer getWrongCount() { return wrongCount; }
    public void setWrongCount(Integer wrongCount) { this.wrongCount = wrongCount; }

    public Integer getMastered() { return mastered; }
    public void setMastered(Integer mastered) { this.mastered = mastered; }

    public String getLastWrongAnswer() { return lastWrongAnswer; }
    public void setLastWrongAnswer(String lastWrongAnswer) { this.lastWrongAnswer = lastWrongAnswer; }
}
