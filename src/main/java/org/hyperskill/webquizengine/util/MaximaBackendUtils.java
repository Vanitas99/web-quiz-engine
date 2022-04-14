package org.hyperskill.webquizengine.util;

import org.hyperskill.webquizengine.dto.MaximaExpressionTuple;
import org.hyperskill.webquizengine.dto.Calculations;
import org.hyperskill.webquizengine.dto.CalculationResponse;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class MaximaBackendUtils {

    private MaximaBackendUtils() {}

    // The captured maxima output with goemaxima starts at input 9
    private static int parseStartIdx = 9;

    public static ArrayList<String> parseMaximaOutputTex(final String output, final long offsetToTexInputs,  Logger logger) {
        var texOutputs = new ArrayList<String>();
        var scanner = new Scanner(output);
        while (scanner.hasNextLine()) {
            var outputLine = scanner.nextLine().replace(" ","");
            if (outputLine.length() < 5 )
                continue;

            var outputMatcher = Pattern.compile("\\(%o(\\d+)\\)(.*)").matcher(outputLine);
            if (outputMatcher.find()) {
                var outputExpression = outputMatcher.group(2);
                var trueId = outputMatcher.group(1);
                // Everything after the parseStartIdx + offsetToTexInputs index is output that we care about.
                if (Integer.parseInt(trueId) >= (parseStartIdx + offsetToTexInputs)) {
                    texOutputs.add(outputExpression);
                }
            }
        }
        return texOutputs;
    }

    public static CalculationResponse parseMaximaOutput(final String output, final Calculations calculations, int initialOffset, Logger logger) {

        var outputLines = new HashMap<String, String>();
        var scanner = new Scanner(output);
        while (scanner.hasNextLine()) {
            var outputLine = scanner.nextLine().replace(" ","");
            if (outputLine.length() < 5 )
                continue;

            var outputMatcher = Pattern.compile("\\(%o(\\d+)\\)(.*)").matcher(outputLine);
            if (outputMatcher.find()) {
                var outputExpression = outputMatcher.group(2);
                var expressionMatcher = Pattern.compile("(\\w+)=(.+)[,\\]]").matcher(outputExpression);
                var trueId = outputMatcher.group(1);
                outputLines.put(trueId, outputExpression);
                String expression = null;
                while (expressionMatcher.find()) {
                    logger.info(expressionMatcher.group(2));
                }
            }
        }

        var response = new CalculationResponse();
        calculations.getOutputVariableNames().forEach(variableName -> {
            int inputSize = calculations.getInputs().size();
            var inputObj = calculations.getInputs().stream().filter(input -> input.getVariableName().equals(variableName)).findFirst().orElseThrow();
            int idx = calculations.getInputs().indexOf(inputObj);
            var outputId = String.valueOf(idx + 9 + initialOffset);

            var tuple = new MaximaExpressionTuple();
            tuple.setVariableName(variableName);
            tuple.setMaximaExpression(outputLines.get(outputId));
            response.addOutputTuple(tuple);
        });
        return response;
    }
}
