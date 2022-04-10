package org.hyperskill.webquizengine.util;

import javax.validation.constraints.NotEmpty;

public class MaximaTuple {
    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getMaximaExpression() {
        return maximaExpression;
    }

    public void setMaximaExpression(String maximaCalculation) {
        this.maximaExpression = maximaCalculation;
    }

    @NotEmpty
    private String variableName;
    @NotEmpty
    private String maximaExpression;
}