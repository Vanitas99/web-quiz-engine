package org.hyperskill.webquizengine.dto;

import org.hyperskill.webquizengine.util.AnswersDto;

import java.util.List;

public class MaximaQuestionReturnDto {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AnswersDto> getAnswerVariables() {
        return answerVariables;
    }

    public void setAnswerVariables(List<AnswersDto> answerVariables) {
        this.answerVariables = answerVariables;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    private Long id;
    private Long seed;
    private String description;
    private List<AnswersDto> answerVariables;
}
