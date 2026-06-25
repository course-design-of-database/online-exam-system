package org.bishe.question.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.bishe.question.entity.QuestionTypeEntity;
import java.util.List;

@Mapper
public interface QuestionTypeMapper {
    QuestionTypeEntity findById(Long id);
    QuestionTypeEntity findByName(String name);
    List<QuestionTypeEntity> findAll();
}
