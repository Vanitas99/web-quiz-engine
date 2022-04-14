package org.hyperskill.webquizengine.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class Calculations {

    @NotEmpty
    @Valid
    private List inputs = new ArrayList<>();
    @NotEmpty
    private List<String> outputVariableNames = new ArrayList<>();

    public List<String> getOutputVariableNames() {
        return outputVariableNames;
    }

    public void setOutputVariableNames(List<String> outputVariableNames) {
        this.outputVariableNames = outputVariableNames;
    }

    public List<MaximaExpressionTuple> getInputs() {
        return inputs;
    }

    public void setInputs(List<MaximaExpressionTuple> inputs) {
        this.inputs = inputs;
    }
}
