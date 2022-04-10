package org.hyperskill.webquizengine.dto;

import org.hyperskill.webquizengine.util.MaximaTuple;

import java.util.ArrayList;
import java.util.List;

public class CalculationResponse {

    public List<MaximaTuple> getVariables() {
        return variables;
    }

    public void setVariables(List<MaximaTuple> variables) {
        this.variables = variables;
    }

    public void addOutputTuple(final MaximaTuple tuple) {
        this.variables.add(tuple);
    }

    private List<MaximaTuple> variables = new ArrayList<>();

}
