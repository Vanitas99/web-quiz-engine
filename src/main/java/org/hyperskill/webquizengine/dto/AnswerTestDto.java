package org.hyperskill.webquizengine.dto;

import org.hyperskill.webquizengine.model.AnswerTestType;

public class AnswerTestDto {
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

    public AnswerTestType getType() {
        return type;
    }

    public void setType(AnswerTestType type) {
        this.type = type;
    }

    private Long id;
    private String name;
    private AnswerTestType type;
}
