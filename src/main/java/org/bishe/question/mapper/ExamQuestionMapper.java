package org.bishe.question.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.bishe.question.entity.ExamQuestion;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ExamQuestionMapper {
    ExamQuestion findById(Long id);
    List<ExamQuestion> findAll();
    int insert(ExamQuestion examQuestion);
    int update(ExamQuestion examQuestion);
    int deleteById(Long id);
    long count();

    List<ExamQuestion> findByExamId(@Param("examId") Long examId);
    List<ExamQuestion> findByExamIdOrderByQuestionOrder(@Param("examId") Long examId);
    List<ExamQuestion> findByQuestionId(@Param("questionId") Long questionId);
    ExamQuestion findByExamIdAndQuestionId(@Param("examId") Long examId, @Param("questionId") Long questionId);
    ExamQuestion findByExamIdAndQuestionOrder(@Param("examId") Long examId, @Param("questionOrder") Integer questionOrder);
    Long countByExamId(@Param("examId") Long examId);
    Long countByQuestionId(@Param("questionId") Long questionId);
    BigDecimal calculateTotalScoreByExamId(@Param("examId") Long examId);
    Integer getMaxQuestionOrderByExamId(@Param("examId") Long examId);
    boolean existsByExamIdAndQuestionId(@Param("examId") Long examId, @Param("questionId") Long questionId);
    boolean existsByExamIdAndQuestionOrder(@Param("examId") Long examId, @Param("questionOrder") Integer questionOrder);
    int deleteByExamId(@Param("examId") Long examId);
    int deleteByQuestionId(@Param("questionId") Long questionId);
    int deleteByExamIdAndQuestionId(@Param("examId") Long examId, @Param("questionId") Long questionId);
    int updateQuestionOrder(@Param("examId") Long examId, @Param("questionId") Long questionId, @Param("newOrder") Integer newOrder);
    int updateQuestionScore(@Param("examId") Long examId, @Param("questionId") Long questionId, @Param("score") BigDecimal score);
    int adjustQuestionOrderAfterDelete(@Param("examId") Long examId, @Param("deletedOrder") Integer deletedOrder);
    List<ExamQuestion> findByExamIdAndOrderRange(@Param("examId") Long examId, @Param("startOrder") Integer startOrder, @Param("endOrder") Integer endOrder);
    List<ExamQuestion> findHighestScoreQuestionsByExamId(@Param("examId") Long examId);
    List<ExamQuestion> findLowestScoreQuestionsByExamId(@Param("examId") Long examId);
}
