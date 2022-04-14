package org.hyperskill.webquizengine.dto;

import java.util.ArrayList;
import java.util.List;

public class CalculationResponse {

    public List<MaximaExpressionTuple> getVariables() {
        return variables;
    }

    public void setVariables(List<MaximaExpressionTuple> variables) {
        this.variables = variables;
    }

    public void addOutputTuple(final MaximaExpressionTuple tuple) {
        this.variables.add(tuple);
    }

    private List<MaximaExpressionTuple> variables = new ArrayList<>();

}
