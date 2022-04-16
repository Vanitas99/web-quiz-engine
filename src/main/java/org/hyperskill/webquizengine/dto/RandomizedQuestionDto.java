package org.hyperskill.webquizengine.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RandomizedQuestionDto implements Serializable {
    private  Long id;
    private  String description;
    private  Long templateQuestionId;
    private  String templateQuestionCategory;
    private  List<AnswerDto> templateQuestionAnswers = new ArrayList<>();
    private  long seed;

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Long getTemplateQuestionId() {
        return templateQuestionId;
    }

    public String getTemplateQuestionCategory() {
        return templateQuestionCategory;
    }

    public List<AnswerDto> getTemplateQuestionAnswers() {
        return templateQuestionAnswers;
    }

    public long getSeed() {
        return seed;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTemplateQuestionId(Long templateQuestionId) {
        this.templateQuestionId = templateQuestionId;
    }

    public void setTemplateQuestionCategory(String templateQuestionCategory) {
        this.templateQuestionCategory = templateQuestionCategory;
    }

    public void setTemplateQuestionAnswers(List<AnswerDto> templateQuestionAnswers) {
        this.templateQuestionAnswers = templateQuestionAnswers;
    }

    public void addTemplateQuestionAnswer(AnswerDto templateQuestionAnswer) {
        this.templateQuestionAnswers.add(templateQuestionAnswer);
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "filledInDescription = " + description + ", " +
                "templateQuestionId = " + templateQuestionId + ", " +
                "templateQuestionCategory = " + templateQuestionCategory + ", " +
                "templateQuestionAnswers = " + templateQuestionAnswers + ", " +
                "seed = " + seed + ")";
    }
}
