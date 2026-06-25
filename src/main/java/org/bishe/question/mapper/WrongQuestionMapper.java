package org.bishe.question.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.bishe.question.entity.WrongQuestion;

import java.util.List;

@Mapper
public interface WrongQuestionMapper {
    WrongQuestion findById(Long id);
    List<WrongQuestion> findByStudentId(@Param("studentId") Long studentId);
    List<WrongQuestion> findByStudentIdAndMastered(@Param("studentId") Long studentId, @Param("mastered") Integer mastered);
    WrongQuestion findByStudentIdAndQuestionId(@Param("studentId") Long studentId, @Param("questionId") Long questionId);
    int insert(WrongQuestion wrongQuestion);
    int update(WrongQuestion wrongQuestion);
    int deleteById(Long id);
    int markMastered(@Param("studentId") Long studentId, @Param("questionId") Long questionId, @Param("mastered") Integer mastered);
    long countByStudentId(@Param("studentId") Long studentId);
    long countByStudentIdAndMastered(@Param("studentId") Long studentId, @Param("mastered") Integer mastered);
    List<WrongQuestion> findByStudentIdWithPagination(@Param("studentId") Long studentId, @Param("mastered") Integer mastered);
}
