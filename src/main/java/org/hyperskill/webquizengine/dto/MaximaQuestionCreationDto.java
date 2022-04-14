package org.hyperskill.webquizengine.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

public class MaximaQuestionCreationDto implements Serializable {

    private Long id;
    @NotBlank(message = "Description must be set for a question")
    private String description;
    @Valid
    private Calculations calculations;

    public List<MaximaExpressionTuple> getAnswers() {
        return answers;
    }

    public void setAnswers(List<MaximaExpressionTuple> answers) {
        this.answers = answers;
    }

    private List<MaximaExpressionTuple> answers;

    public Calculations getCalculations() {
        return calculations;
    }

    public void setCalculations(Calculations calculations) {
        this.calculations = calculations;
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
