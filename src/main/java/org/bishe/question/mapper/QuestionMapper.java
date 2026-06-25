package org.bishe.question.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.bishe.question.entity.Question;
import java.util.List;

@Mapper
public interface QuestionMapper {
    Question findById(Long id);
    List<Question> findAll();
    int insert(Question question);
    int update(Question question);
    int deleteById(Long id);
    boolean existsById(Long id);
    long count();

    List<Question> findByStatus(@Param("status") Integer status);
    List<Question> findByCreatedBy(@Param("createdBy") Long createdBy);
    List<Question> searchByKeyword(@Param("keyword") String keyword);
    List<Question> searchByKeywordIncludingTags(@Param("keyword") String keyword);
    List<Question> searchQuestions(@Param("keyword") String keyword, @Param("type") String type, @Param("difficulty") String difficulty);
    List<java.util.Map<String, Object>> countByType();
    List<java.util.Map<String, Object>> countByDifficulty();
    List<Question> findRecentQuestions();
    List<Question> findByTag(@Param("tag") String tag);
    boolean existsByTitle(String title);
    boolean existsByTitleAndIdNot(@Param("title") String title, @Param("id") Long id);
    List<Question> findByStatusOrderByIdAsc(@Param("status") Integer status);
    Long countSingleType(@Param("type") String type);
    Long countSingleDifficulty(@Param("difficulty") String difficulty);
}
