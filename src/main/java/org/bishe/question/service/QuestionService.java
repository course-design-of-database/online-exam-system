package org.bishe.question.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.bishe.question.dto.QuestionRequest;
import org.bishe.question.dto.QuestionResponse;
import org.bishe.question.entity.Question;
import org.bishe.question.entity.QuestionTypeEntity;
import org.bishe.question.mapper.QuestionMapper;
import org.bishe.question.mapper.QuestionTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目服务类
 */
@Service
@Transactional
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionTypeMapper questionTypeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Type cache: maps between typeId (DB) and QuestionType enum
    private volatile Map<Long, Question.QuestionType> typeIdToEnumCache;
    private volatile Map<Question.QuestionType, Long> enumToTypeIdCache;

    private void initTypeCache() {
        if (typeIdToEnumCache == null) {
            synchronized (this) {
                if (typeIdToEnumCache == null) {
                    List<QuestionTypeEntity> types = questionTypeMapper.findAll();
                    Map<Long, Question.QuestionType> idMap = new HashMap<>();
                    Map<Question.QuestionType, Long> enumMap = new HashMap<>();
                    for (QuestionTypeEntity entity : types) {
                        try {
                            Question.QuestionType qt = Question.QuestionType.valueOf(entity.getName());
                            idMap.put(entity.getId(), qt);
                            enumMap.put(qt, entity.getId());
                        } catch (IllegalArgumentException e) {
                            System.err.println("未知题目类型: " + entity.getName());
                        }
                    }
                    this.typeIdToEnumCache = idMap;
                    this.enumToTypeIdCache = enumMap;
                }
            }
        }
    }

    private void populateTypeEnum(Question question) {
        if (question != null && question.getTypeId() != null && question.getType() == null) {
            initTypeCache();
            Question.QuestionType qt = typeIdToEnumCache.get(question.getTypeId());
            if (qt != null) {
                question.setType(qt);
            }
        }
    }

    public QuestionResponse createQuestion(QuestionRequest questionRequest, Long createdBy) {
        System.out.println("=== 创建题目开始 ===");
        System.out.println("题目标题: " + questionRequest.getTitle());
        System.out.println("题目类型: " + questionRequest.getType());
        System.out.println("难度等级: " + questionRequest.getDifficulty());

        if (questionMapper.existsByTitle(questionRequest.getTitle())) {
            throw new RuntimeException("题目标题已存在");
        }

        Question question = new Question();
        question.setTitle(questionRequest.getTitle());
        question.setContent(questionRequest.getContent());
        question.setType(questionRequest.getType());
        question.setDifficulty(questionRequest.getDifficulty());
        question.setStatus(questionRequest.getStatus());
        question.setCreatedBy(createdBy);

        if (questionRequest.getOptions() != null && !questionRequest.getOptions().isEmpty()) {
            try {
                question.setOptions(objectMapper.writeValueAsString(questionRequest.getOptions()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("选项数据格式错误", e);
            }
        }

        if (questionRequest.getCorrectAnswers() != null && !questionRequest.getCorrectAnswers().isEmpty()) {
            try {
                question.setCorrectAnswers(objectMapper.writeValueAsString(questionRequest.getCorrectAnswers()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("正确答案数据格式错误", e);
            }
        }

        if (StringUtils.hasText(questionRequest.getCorrectAnswer())) {
            question.setCorrectAnswer(questionRequest.getCorrectAnswer());
        }

        if (questionRequest.getTags() != null && !questionRequest.getTags().isEmpty()) {
            try {
                question.setTags(objectMapper.writeValueAsString(questionRequest.getTags()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("标签数据格式错误", e);
            }
        }

        initTypeCache();
        question.setTypeId(enumToTypeIdCache.get(question.getType()));
        questionMapper.insert(question);
        System.out.println("题目创建成功，ID: " + question.getId());

        return convertToResponse(question);
    }

    public QuestionResponse updateQuestion(Long id, QuestionRequest questionRequest) {
        System.out.println("=== 更新题目开始 ===");
        System.out.println("题目ID: " + id);

        Question question = questionMapper.findById(id);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }

        if (questionMapper.existsByTitleAndIdNot(questionRequest.getTitle(), id)) {
            throw new RuntimeException("题目标题已存在");
        }

        question.setTitle(questionRequest.getTitle());
        question.setContent(questionRequest.getContent());
        question.setType(questionRequest.getType());
        question.setDifficulty(questionRequest.getDifficulty());
        question.setStatus(questionRequest.getStatus());

        if (questionRequest.getOptions() != null && !questionRequest.getOptions().isEmpty()) {
            try {
                question.setOptions(objectMapper.writeValueAsString(questionRequest.getOptions()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("选项数据格式错误", e);
            }
        } else {
            question.setOptions(null);
        }

        if (questionRequest.getCorrectAnswers() != null && !questionRequest.getCorrectAnswers().isEmpty()) {
            try {
                question.setCorrectAnswers(objectMapper.writeValueAsString(questionRequest.getCorrectAnswers()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("正确答案数据格式错误", e);
            }
        } else {
            question.setCorrectAnswers(null);
        }

        question.setCorrectAnswer(questionRequest.getCorrectAnswer());

        if (questionRequest.getTags() != null && !questionRequest.getTags().isEmpty()) {
            try {
                question.setTags(objectMapper.writeValueAsString(questionRequest.getTags()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("标签数据格式错误", e);
            }
        } else {
            question.setTags(null);
        }

        initTypeCache();
        question.setTypeId(enumToTypeIdCache.get(question.getType()));
        questionMapper.update(question);
        System.out.println("题目更新成功");

        return convertToResponse(question);
    }

    public Question saveQuestion(Question question) {
        initTypeCache();
        if (question.getType() != null && question.getTypeId() == null) {
            question.setTypeId(enumToTypeIdCache.get(question.getType()));
        }
        questionMapper.insert(question);
        return question;
    }

    public void deleteQuestion(Long id) {
        System.out.println("=== 删除题目开始 ===");
        System.out.println("题目ID: " + id);

        if (!questionMapper.existsById(id)) {
            throw new RuntimeException("题目不存在");
        }

        questionMapper.deleteById(id);
        System.out.println("题目删除成功");
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long id) {
        Question question = questionMapper.findById(id);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }
        populateTypeEnum(question);
        return convertToResponse(question);
    }

    @Transactional(readOnly = true)
    public Question getQuestionEntityById(Long id) {
        Question question = questionMapper.findById(id);
        if (question != null) {
            populateTypeEnum(question);
        }
        return question;
    }

    @Transactional(readOnly = true)
    public PageInfo<QuestionResponse> getQuestions(int page, int size, String keyword,
                                                   Question.QuestionType type, Question.Difficulty difficulty) {
        System.out.println("=== 分页查询题目 ===");
        System.out.println("页码: " + page + ", 大小: " + size);
        System.out.println("关键词: " + keyword + ", 类型: " + type + ", 难度: " + difficulty);

        String typeStr = type != null ? type.name() : null;
        String diffStr = difficulty != null ? difficulty.name() : null;

        PageHelper.startPage(page + 1, size, "id asc");

        List<Question> questionList;
        if (StringUtils.hasText(keyword) || type != null || difficulty != null) {
            questionList = questionMapper.searchQuestions(keyword, typeStr, diffStr);
        } else {
            questionList = questionMapper.findByStatus(1);
        }

        PageInfo<Question> questionPage = new PageInfo<>(questionList);
        List<QuestionResponse> responseList = questionPage.getList().stream()
                .peek(this::populateTypeEnum)
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        PageInfo<QuestionResponse> result = new PageInfo<>();
        result.setList(responseList);
        result.setTotal(questionPage.getTotal());
        result.setPageNum(questionPage.getPageNum());
        result.setPageSize(questionPage.getPageSize());
        result.setPages(questionPage.getPages());
        return result;
    }

    @Transactional(readOnly = true)
    public List<Question> getQuestionsForExport(String keyword, Question.QuestionType type, Question.Difficulty difficulty) {
        System.out.println("=== 获取题目用于导出 ===");
        String typeStr = type != null ? type.name() : null;
        String diffStr = difficulty != null ? difficulty.name() : null;

        if (StringUtils.hasText(keyword) || type != null || difficulty != null) {
            List<Question> questions = questionMapper.searchQuestions(keyword, typeStr, diffStr);
            questions.forEach(this::populateTypeEnum);
            return questions;
        } else {
            List<Question> questions = questionMapper.findByStatusOrderByIdAsc(1);
            questions.forEach(this::populateTypeEnum);
            return questions;
        }
    }

    @Transactional(readOnly = true)
    public Object getQuestionStats() {
        long totalCount = questionMapper.count();
        List<?> typeStats = questionMapper.countByType();
        List<?> difficultyStats = questionMapper.countByDifficulty();

        return new Object() {
            public final long total = totalCount;
            public final List<?> byType = typeStats;
            public final List<?> byDifficulty = difficultyStats;
        };
    }

    private QuestionResponse convertToResponse(Question question) {
        QuestionResponse response = new QuestionResponse(question);

        try {
            if (StringUtils.hasText(question.getOptions())) {
                List<String> options = objectMapper.readValue(question.getOptions(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                response.setOptions(options);
            }

            if (StringUtils.hasText(question.getCorrectAnswers())) {
                try {
                    List<Object> rawAnswers = objectMapper.readValue(question.getCorrectAnswers(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class));
                    List<String> correctAnswers = rawAnswers.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
                    response.setCorrectAnswers(correctAnswers);
                } catch (Exception e) {
                    List<String> correctAnswers = objectMapper.readValue(question.getCorrectAnswers(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                    response.setCorrectAnswers(correctAnswers);
                }
            }

            if (StringUtils.hasText(question.getTags())) {
                List<String> tags = objectMapper.readValue(question.getTags(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                response.setTags(tags);
            }
        } catch (JsonProcessingException e) {
            System.err.println("JSON解析错误: " + e.getMessage());
        }

        return response;
    }
}