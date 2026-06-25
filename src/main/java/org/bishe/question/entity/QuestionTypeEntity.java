package org.bishe.question.entity;

import java.io.Serializable;

/**
 * 题目类型字典实体类
 * 对应 question_types 表
 */
public class QuestionTypeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String displayName;

    public QuestionTypeEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
