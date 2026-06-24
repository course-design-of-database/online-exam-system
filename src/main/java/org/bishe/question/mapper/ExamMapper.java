package org.bishe.question.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.bishe.question.entity.Exam;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ExamMapper {
    Exam findById(Long id);
    List<Exam> findAll();
    int insert(Exam exam);
    int update(Exam exam);
    int deleteById(Long id);
    boolean existsById(Long id);
    long count();

    List<Exam> findByStatus(@Param("status") Integer status);
    List<Exam> findByCreatedBy(@Param("createdBy") Long createdBy);
    List<Exam> findByTitleContaining(@Param("title") String title);
    List<Exam> findActiveExams(@Param("now") LocalDateTime now);
    List<Exam> findUpcomingExams(@Param("now") LocalDateTime now);
    List<Exam> findFinishedExams(@Param("now") LocalDateTime now);
    List<Exam> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    List<Exam> findAvailableExamsForStudent(@Param("studentId") Long studentId, @Param("now") LocalDateTime now);
    List<Exam> findExamsByStudent(@Param("studentId") Long studentId);
    Long countByStatus(@Param("status") Integer status);
    Long countByCreatedBy(Long createdBy);
    List<Exam> findTop5ByOrderByCreatedAtDesc();
    List<Exam> findRecentExams();
    Exam findByIdAndCreatedBy(@Param("id") Long id, @Param("createdBy") Long createdBy);
    boolean existsByTitle(String title);
    boolean existsByTitleAndIdNot(@Param("title") String title, @Param("excludeId") Long excludeId);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);
    int updateExpiredExamsStatus(@Param("now") LocalDateTime now, @Param("updateTime") LocalDateTime updateTime);
}
