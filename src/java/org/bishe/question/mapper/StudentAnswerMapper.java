package org.bishe.question.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.bishe.question.entity.StudentAnswer;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface StudentAnswerMapper {
    StudentAnswer findById(Long id);
    List<StudentAnswer> findAll();
    int insert(StudentAnswer studentAnswer);
    int update(StudentAnswer studentAnswer);
    int deleteById(Long id);
    long count();

    List<StudentAnswer> findByExamRecordId(@Param("examRecordId") Long examRecordId);
    List<StudentAnswer> findByExamRecordIdOrderByQuestionId(@Param("examRecordId") Long examRecordId);
    List<StudentAnswer> findByQuestionId(@Param("questionId") Long questionId);
    StudentAnswer findByExamRecordIdAndQuestionId(@Param("examRecordId") Long examRecordId, @Param("questionId") Long questionId);
    Long countByExamRecordId(@Param("examRecordId") Long examRecordId);
    Long countByQuestionId(@Param("questionId") Long questionId);
    Long countCorrectByExamRecordId(@Param("examRecordId") Long examRecordId);
    Long countWrongByExamRecordId(@Param("examRecordId") Long examRecordId);
    Long countUnansweredByExamRecordId(@Param("examRecordId") Long examRecordId);
    Double getCorrectRateByQuestionId(@Param("questionId") Long questionId);
    List<StudentAnswer> findAnsweredByExamRecordId(@Param("examRecordId") Long examRecordId);
    List<StudentAnswer> findUnansweredByExamRecordId(@Param("examRecordId") Long examRecordId);
    List<StudentAnswer> findCorrectByExamRecordId(@Param("examRecordId") Long examRecordId);
    List<StudentAnswer> findWrongByExamRecordId(@Param("examRecordId") Long examRecordId);
    boolean existsByExamRecordIdAndQuestionId(@Param("examRecordId") Long examRecordId, @Param("questionId") Long questionId);
    List<StudentAnswer> findByAnswerTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    List<StudentAnswer> findRecentAnswers();
    List<StudentAnswer> findRecentAnswersByExamRecord(@Param("examRecordId") Long examRecordId);
    List<StudentAnswer> findRecentAnswersByQuestion(@Param("questionId") Long questionId);
    int updateStudentAnswer(@Param("examRecordId") Long examRecordId, @Param("questionId") Long questionId, @Param("studentAnswer") String studentAnswer, @Param("answerTime") LocalDateTime answerTime, @Param("updateTime") LocalDateTime updateTime);
    int updateCorrectness(@Param("examRecordId") Long examRecordId, @Param("questionId") Long questionId, @Param("isCorrect") Boolean isCorrect, @Param("updateTime") LocalDateTime updateTime);
    int updateCorrectAnswer(@Param("examRecordId") Long examRecordId, @Param("questionId") Long questionId, @Param("correctAnswer") String correctAnswer, @Param("updateTime") LocalDateTime updateTime);
    int clearAnswer(@Param("examRecordId") Long examRecordId, @Param("questionId") Long questionId, @Param("updateTime") LocalDateTime updateTime);
    int deleteByExamRecordId(@Param("examRecordId") Long examRecordId);
    int deleteByQuestionId(@Param("questionId") Long questionId);
    int deleteByExamRecordIdAndQuestionId(@Param("examRecordId") Long examRecordId, @Param("questionId") Long questionId);
    List<Object[]> getAnswerDistributionByQuestionId(@Param("questionId") Long questionId);
    List<Object[]> getQuestionStatisticsByExamId(@Param("examId") Long examId);
}
