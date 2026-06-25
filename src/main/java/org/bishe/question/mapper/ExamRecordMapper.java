package org.bishe.question.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.bishe.question.entity.ExamRecord;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ExamRecordMapper {
    ExamRecord findById(Long id);
    List<ExamRecord> findAll();
    int insert(ExamRecord examRecord);
    int update(ExamRecord examRecord);
    int deleteById(Long id);
    long count();

    List<ExamRecord> findByExamId(@Param("examId") Long examId);
    List<ExamRecord> findByStudentId(@Param("studentId") Long studentId);
    ExamRecord findByExamIdAndStudentId(@Param("examId") Long examId, @Param("studentId") Long studentId);
    List<ExamRecord> findByStatus(@Param("status") Integer status);
    List<ExamRecord> findByExamIdAndStatus(@Param("examId") Long examId, @Param("status") Integer status);
    List<ExamRecord> findByStudentNameContaining(@Param("studentName") String studentName);
    List<ExamRecord> findByStudentNameContainingAndStatus(@Param("studentName") String studentName, @Param("status") Integer status);
    List<ExamRecord> findByExamIdAndStudentNameContaining(@Param("examId") Long examId, @Param("studentName") String studentName);
    List<ExamRecord> findByExamIdAndStudentNameContainingAndStatus(@Param("examId") Long examId, @Param("studentName") String studentName, @Param("status") Integer status);
    Long countByExamIdAndStatus(@Param("examId") Long examId, @Param("status") Integer status);
    Long countByExamIdAndStatusAndTotalScoreGreaterThanEqual(@Param("examId") Long examId, @Param("status") Integer status, @Param("minScore") BigDecimal minScore);
    List<ExamRecord> findInProgressRecords();
    List<ExamRecord> findCompletedRecords();
    List<ExamRecord> findCompletedRecordsByStudent(@Param("studentId") Long studentId);
    List<ExamRecord> findCompletedRecordsByExam(@Param("examId") Long examId);
    Long countByExamId(@Param("examId") Long examId);
    Long countCompletedByExamId(@Param("examId") Long examId);
    Long countByStudentId(@Param("studentId") Long studentId);
    Long countCompletedByStudentId(@Param("studentId") Long studentId);
    boolean existsByExamIdAndStudentId(@Param("examId") Long examId, @Param("studentId") Long studentId);
    BigDecimal getAverageScoreByExamId(@Param("examId") Long examId);
    BigDecimal getAverageScoreByExamIdAndStatus(@Param("examId") Long examId, @Param("status") Integer status);
    BigDecimal getMaxScoreByExamId(@Param("examId") Long examId);
    BigDecimal getMaxScoreByExamIdAndStatus(@Param("examId") Long examId, @Param("status") Integer status);
    BigDecimal getMinScoreByExamId(@Param("examId") Long examId);
    BigDecimal getMinScoreByExamIdAndStatus(@Param("examId") Long examId, @Param("status") Integer status);
    BigDecimal getAverageScoreByStudentId(@Param("studentId") Long studentId);
    Long countPassedByExamId(@Param("examId") Long examId);
    Long countFailedByExamId(@Param("examId") Long examId);
    List<ExamRecord> findTimeoutRecords();
    List<ExamRecord> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    List<ExamRecord> findRecentRecords();
    List<ExamRecord> findRecentRecordsByStudent(@Param("studentId") Long studentId);
    List<ExamRecord> findRecentExamRecordsByStudentId(@Param("studentId") Long studentId);
    List<ExamRecord> findRecentRecordsByExam(@Param("examId") Long examId);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);
    int updateScore(@Param("id") Long id, @Param("score") BigDecimal score, @Param("correctCount") Integer correctCount, @Param("wrongCount") Integer wrongCount, @Param("unansweredCount") Integer unansweredCount, @Param("updateTime") LocalDateTime updateTime);
    int timeoutInProgressRecords(@Param("submitTime") LocalDateTime submitTime, @Param("updateTime") LocalDateTime updateTime);
    int deleteByExamId(@Param("examId") Long examId);
    int deleteByStudentId(@Param("studentId") Long studentId);
    Long countByStatus(@Param("status") Integer status);
    List<ExamRecord> findTop10ByOrderByCreatedAtDesc();
    List<ExamRecord> findByCreatedAtAfter(@Param("createdAt") LocalDateTime createdAt);
    Long countByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    Long countByStatusAndSubmitTimeBetween(@Param("status") Integer status, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
