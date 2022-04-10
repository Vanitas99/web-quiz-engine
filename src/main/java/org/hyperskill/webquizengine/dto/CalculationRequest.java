package org.hyperskill.webquizengine.dto;

import org.hyperskill.webquizengine.util.AnswersDto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class CalculationRequest {

    @NotEmpty
    @Valid
    private List<AnswersDto> inputs = new ArrayList<>();
    @NotEmpty
    private List<String> outputVariableNames = new ArrayList<>();

    public List<String> getOutputVariableNames() {
        return outputVariableNames;
    }

    public void setOutputVariableNames(List<String> outputVariableNames) {
        this.outputVariableNames = outputVariableNames;
    }

    public List<AnswersDto> getInputs() {
        return inputs;
    }

    public void setInputs(List<AnswersDto> inputs) {
        this.inputs = inputs;
    }
}
