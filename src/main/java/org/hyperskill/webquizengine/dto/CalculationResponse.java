package org.hyperskill.webquizengine.dto;

import org.hyperskill.webquizengine.util.AnswersDto;

import java.util.ArrayList;
import java.util.List;

public class CalculationResponse {

    public List<AnswersDto> getVariables() {
        return variables;
    }

    public void setVariables(List<AnswersDto> variables) {
        this.variables = variables;
    }

    public void addOutputTuple(final AnswersDto tuple) {
        this.variables.add(tuple);
    }

    private List<AnswersDto> variables = new ArrayList<>();

}
