package org.hyperskill.webquizengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperskill.webquizengine.model.Answer;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MaximaQuestionTemplate implements Serializable {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @NotBlank(message = "Description must be set for a question")
    private String description;
    private String category;

    @Valid
    private ArrayList<MaximaExpressionTuple> calculations = new ArrayList<MaximaExpressionTuple>();
    private ArrayList<AnswerDto> answers = new ArrayList<AnswerDto>();

    private boolean globalSimplification;

    public boolean isGlobalSimplification() {
        return globalSimplification;
    }

    public void setGlobalSimplification(boolean globalSimplification) {
        this.globalSimplification = globalSimplification;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ArrayList<MaximaExpressionTuple> getCalculations() {
        return calculations;
    }

    public void setCalculations(ArrayList<MaximaExpressionTuple> calculations) {
        this.calculations = calculations;
    }

    public ArrayList<AnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<AnswerDto> answers) {
        this.answers = answers;
    }

    public void addAnswer(AnswerDto answer) {
        this.answers.add(answer);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }


}
