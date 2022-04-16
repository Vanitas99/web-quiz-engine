package org.hyperskill.webquizengine.dto;

import java.util.ArrayList;

public class CalculationRequest {
    private ArrayList<MaximaExpressionTuple> expressions;
    private ArrayList<String> outputVariables;
    private boolean globalSimp;

    public boolean isGlobalSimp() {
        return globalSimp;
    }

    public void setGlobalSimp(boolean globalSimp) {
        this.globalSimp = globalSimp;
    }

    public ArrayList<MaximaExpressionTuple> getExpressions() {
        return expressions;
    }

    public void setExpressions(ArrayList<MaximaExpressionTuple> expressions) {
        this.expressions = expressions;
    }

    public ArrayList<String> getOutputVariables() {
        return outputVariables;
    }

    public void setOutputVariables(ArrayList<String> outputVariables) {
        this.outputVariables = outputVariables;
    }
}
