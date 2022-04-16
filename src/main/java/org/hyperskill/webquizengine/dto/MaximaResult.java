package org.hyperskill.webquizengine.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.*;

public class MaximaResult {
    private String answerName;
    @JsonInclude(Include.NON_NULL)
    private String feedback;
    private boolean correct;
    @JsonInclude(Include.NON_NULL)
    private String latexFeedback;

    public static MaximaResult success(String answerName) {
        var result = new MaximaResult();
        result.answerName = answerName;
        result.correct = true;
        return result;
    }

    public static MaximaResult failure(String answerName, String feedback, String latexFeedback) {
        var result = new MaximaResult();
        result.answerName = answerName;
        result.correct = false;
        result.feedback = feedback;
        result.latexFeedback = latexFeedback;
        return result;
    }

    public static MaximaResult failure(String answerName, String feedback) {
        return failure(answerName, feedback, null);
    }

    public String getAnswerName() {
        return answerName;
    }

    public void setAnswerName(String answerName) {
        this.answerName = answerName;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getLatexFeedback() {
        return latexFeedback;
    }

    public void setLatexFeedback(String latexFeedback) {
        this.latexFeedback = latexFeedback;
    }
}
