package org.hyperskill.webquizengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;


public class AnswerDto implements Serializable {
    private Long id;
    private String name;
    private String prompt;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String assessmentFunction;

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void setAssessmentFunction(String assessmentFunction) {
        this.assessmentFunction = assessmentFunction;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getAssessmentFunction() {
        return assessmentFunction;
    }
}
