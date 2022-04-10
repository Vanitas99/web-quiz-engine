package org.hyperskill.webquizengine.dto;

import org.hyperskill.webquizengine.util.AnswersDto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class MaximaQuestionCreationDto implements Serializable {

    private Long id;
    @NotBlank(message = "Description must be set for a question")
    private String description;
    @Valid
    private CalculationRequest calculations;

    public List<AnswersDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswersDto> answers) {
        this.answers = answers;
    }

    private List<AnswersDto> answers;

    public CalculationRequest getCalculations() {
        return calculations;
    }

    public void setCalculations(CalculationRequest calculations) {
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
