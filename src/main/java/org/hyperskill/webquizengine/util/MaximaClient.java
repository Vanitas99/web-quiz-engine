package org.hyperskill.webquizengine.util;

import org.hyperskill.webquizengine.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.validation.constraints.Max;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class MaximaClient {
    private final BackendProperties props;
    private final URI maximaUri;
    private final OkHttp3ClientHttpRequestFactory http = new OkHttp3ClientHttpRequestFactory();
    private final Logger logger = LoggerFactory.getLogger(MaximaClient.class);

    // The output of stack maxima starts with (%i9), so we start parsing there
    private static final short OUTPUT_START_IDX = 9;
    private static String  PLOT_URL_BASE = "";
    private static final int TIMEOUT = 1000;

    public static class CalculationProperties {
        private String calculations;
        private long numberOfCalculations;
        private long seed;
        private boolean simp;

        public CalculationProperties(String calculations, long numberOfCalculations, long seed, boolean simp) {
            this.calculations = calculations;
            this.numberOfCalculations = numberOfCalculations;
            this.seed = seed;
            this.simp = simp;
        }
    }

    public MaximaClient(BackendProperties props) {
        this.props = props;
        this.maximaUri = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(props.getIp())
                .port(props.getPort())
                .replacePath("/maxima").build().toUri();
    }

    public CalculationResponse calculate(final CalculationProperties props,
                                         final ArrayList<String> outputVariables) {

        var commandsBuilder = new StringBuilder();
        outputVariables.forEach(variable -> {
            commandsBuilder.append(String.format("%s;", variable));
        });

        var maximaOutput = executeMaxima(props.calculations,props.numberOfCalculations, commandsBuilder.toString(), props.seed, props.simp);
        var calcResponse = new CalculationResponse();
        for (int idx = 0; idx < maximaOutput.size(); idx++ ) {
            var tuple = new MaximaExpressionTuple();
            tuple.setVariableName(outputVariables.get(idx));
            tuple.setMaximaExpression(maximaOutput.get(idx));
            calcResponse.addOutputTuple(tuple);
        }
        return calcResponse;
    }


    public MaximaResult assesUserInput(final CalculationProperties props, final String userAnswer, final String answerVariableName, final String comparisonFunction) {
        var commandsBuilder = new StringBuilder();

        var randomNumber = System.currentTimeMillis() & 0xffff;
        var functionBody = String.format(
                "assessFunction_%d(USER_ANSWER,CORRECT_ANSWER):=block(" +
                        "simp:false,%s" +
                        ");", randomNumber, comparisonFunction);
        var functionCall = String.format("assessFunction_%d(%s,%s);", randomNumber, userAnswer, answerVariableName);
        commandsBuilder.append(functionBody).append(functionCall);

        var results = executeMaxima(props.calculations, props.numberOfCalculations, commandsBuilder.toString(), props.seed, false);
        var output = results.get(1);
        logger.info("Received Output from Maxima {}", output);
        var stackSyntaxMatcher = Pattern.compile("\\[(\\w*),(\\w*),\"(.*)\".*\\]").matcher(output);

        if (stackSyntaxMatcher.find()) {
            var valditiy = stackSyntaxMatcher.group(1);
            var correct = stackSyntaxMatcher.group(2);
            var feedBack = stackSyntaxMatcher.group(3);
            var result = new MaximaResult();
            result.setFeedback(feedBack);
            result.setAnswerName(answerVariableName);
            result.setCorrect(Boolean.parseBoolean(correct));
            return result;
        }
        return null;
    }

    public String getTexOutputs(final CalculationProperties props, final String templatedDescription, final boolean simplifyTexDisplay) {
        var variableNames = new ArrayList<String>();
        var descriptionVariablesToReplace = new ArrayList<String>();
        var varNameMatcher = Pattern.compile("@([a-zA-Z0-9]+)@").matcher(templatedDescription);
        logger.info("Templated Description {}", templatedDescription);
        while (varNameMatcher.find()) {
            var varName = varNameMatcher.group(1);
            logger.info("Found variable {}", varName);
            logger.info("Found whole {}", varNameMatcher.group());

            descriptionVariablesToReplace.add(varNameMatcher.group());
            variableNames.add(varName);
        }

        var commandsBuilder = new StringBuilder();
        variableNames.forEach(variable -> {
            var texCommand = String.format("ev(stack_disp(%s,\"\"), simp:%s);",variable, simplifyTexDisplay);
            commandsBuilder.append(texCommand);
        });
        logger.info("Test {}", commandsBuilder.toString());
        logger.info("Number {}", props.numberOfCalculations);
        var texStrings =  executeMaxima(props.calculations, props.numberOfCalculations, commandsBuilder.toString(), props.seed, props.simp);
        var descriptionWithTex = templatedDescription;
        int i = 0;
        for (String name : descriptionVariablesToReplace) {
            descriptionWithTex = descriptionWithTex.replaceAll(name, String.format("%s", texStrings.get(i).replaceAll("\\\\", "\\")));
            i++;
        }

        return descriptionWithTex;
    }

    private ArrayList<String> executeMaxima(final String calculations,
                                                        final long numberOfCalculations,
                                                        final String commands,
                                                        final long seed,
                                                        boolean globalSimplification) {

        var start = System.nanoTime();
        var prefix = new ArrayList<String>();
        prefix.add(String.format("simp:%b;", globalSimplification));
        prefix.add(String.format("stack_randseed(%d);", seed));

        // questionCalculations
        // Examples for outCommands to execute over the specified calculations
        // outCommands ["answer1Evaluation(SA,TA):=block(....);", "answer1Evaluation(2*x^2, 4*x^3);"]
        // outCommands ["stack_display(variableNamen1, "");", "stack_display(variableNamen1, "");, ...]
        var inputBuilder = new StringBuilder();
        prefix.forEach(inputBuilder::append);
        inputBuilder.append(calculations);
        inputBuilder.append(commands);

        String output = null;
        try {
            var builder = UriComponentsBuilder.fromUri(maximaUri)
                    .queryParam("input", UriUtils.encode(inputBuilder.toString(), StandardCharsets.UTF_8))
                    .queryParam("timeout", TIMEOUT);
            if (PLOT_URL_BASE != null) {
                builder.queryParam("plotUrlBase", UriUtils.encode(PLOT_URL_BASE, StandardCharsets.UTF_8));
            }
            var request = http.createRequest(
                    builder.build(true).toUri(),
                    HttpMethod.GET);
            var response = request.execute();
            logger.info("Sending {} to maxima backend", request.getURI().toString());
            output = new String(response.getBody().readAllBytes());

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        var time = System.nanoTime() - start;
        logger.info("Sending to Maxima Backend took {} ns", time / 1000000);
        return parseMaximaOutput(output, prefix.size() + numberOfCalculations);
    }

    private ArrayList<String> parseMaximaOutput(final String output, final long offsetToOutput) {
        logger.info("Parsing Maxima output {} with offset {}", output, offsetToOutput);
        var start = System.nanoTime();
        var outputLines = new HashMap<String, String>();
        var outputArray = new ArrayList<String>();
        var scanner = new Scanner(output);
        while (scanner.hasNextLine()) {
            var outputLine = scanner.nextLine().replace(" ","");
            if (outputLine.length() < 5 )
                continue;

            var outputMatcher = Pattern.compile("\\(%o(\\d+)\\)(.*)").matcher(outputLine);
            if (outputMatcher.find()) {
                // Only when we are at the specified offset, we wanna store the outputs and return them
                if (Integer.parseInt(outputMatcher.group(1)) >= OUTPUT_START_IDX + offsetToOutput) {
                    var outputExpression = outputMatcher.group(2);
                    outputArray.add(outputExpression);
                }
            }
        }
        var time = System.nanoTime() - start;
        logger.info("Parsing took {} ns!", time / 1000000);
        return outputArray;
    }

    public boolean checkBackendHealth() {
        try {
            var uri = UriComponentsBuilder.fromUri(maximaUri)
                    .query(props.getHealthcheckUrl())
                    .encode(StandardCharsets.UTF_8).build().toUri();
            var res = http.createRequest(uri, HttpMethod.GET).execute();
            return res.getStatusCode().is2xxSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
