package org.hyperskill.webquizengine.service;

import org.hyperskill.webquizengine.dto.CalculationRequest;
import org.hyperskill.webquizengine.dto.CalculationResponse;
import org.hyperskill.webquizengine.dto.MaximaQuestionDto;
import org.hyperskill.webquizengine.dto.ResultDto;
import org.hyperskill.webquizengine.exception.QuizNotFoundException;
import org.hyperskill.webquizengine.exception.UserNotFoundException;
import org.hyperskill.webquizengine.model.MaximaQuestion;
import org.hyperskill.webquizengine.repository.MaximaQuestionRepository;
import org.hyperskill.webquizengine.repository.RandomVariableRepository;
import org.hyperskill.webquizengine.repository.UserRepository;
import org.hyperskill.webquizengine.util.BackendProperties;
import org.hyperskill.webquizengine.util.MaximaTuple;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class MaximaQuestionService {


    public static class MaximaQueryParams {
        @NotBlank
        private String input = null;
        @NotNull
        @Min(value = 100)
        @Max(value = 5000)
        private int timeout;
        private String plotUrlBase = null;
        private String version = null;

        public MaximaQueryParams(final String input) {
             this(input,500);
        }

        public MaximaQueryParams(final String input, int timeout) {
            this.input = input;
            this.timeout = timeout;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public final String getPlotUrlBase() {
            return plotUrlBase;
        }

        public void setPlotUrlBase(final String plotUrlBase) {
            this.plotUrlBase = plotUrlBase;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(final String version) {
            this.version = version;
        }
    }

    private final MaximaQuestionRepository questionRepository;
    private final RandomVariableRepository variableRepository;
    private final UserRepository userRepository;
    private final BackendProperties maximaProps;
    private final OkHttp3ClientHttpRequestFactory http = new OkHttp3ClientHttpRequestFactory();
    private final Logger logger = LoggerFactory.getLogger(MaximaQuestionService.class);
    private final URI maximaUri;


    @Autowired
    public MaximaQuestionService(MaximaQuestionRepository repository,
                                 RandomVariableRepository variableRepository,
                                 UserRepository userRepository,
                                 BackendProperties maximaProps) {
        this.questionRepository = repository;
        this.variableRepository = variableRepository;
        this.userRepository = userRepository;
        this.maximaProps = maximaProps;

        maximaUri = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(maximaProps.getIp())
                .port(maximaProps.getPort())
                .replacePath("/maxima").build().toUri();
    }

    public long createQuestion(final MaximaQuestionDto dto, final String username) {
        var user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        var question = new ModelMapper().map(dto, MaximaQuestion.class);
        logger.info(question.toString());
        var r = new Random();
        float b =  (long) (r.nextFloat()*10.0) / 10.0f;
        logger.info("Random Variable {}", b);
        return questionRepository.save(question).getId();
    }

    public void deleteQuestion(@NotNull final long id, final String username) {
        var user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        try {
            questionRepository.deleteById(id);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            throw new QuizNotFoundException();
        };
    }

    public MaximaQuestion getQuestionById(final long id) {
        return questionRepository.findById(id).orElseThrow(QuizNotFoundException::new);
    }

    public Slice<MaximaQuestion> findQuestionsByCategory(final String category, final int page,  final int size) {
        var pageable = PageRequest.of(page, size, Sort.by("id"));
        return questionRepository.findQuestionsByCategory(category, pageable);
    }

    public ResultDto assessUserAnswer(final String userAnswer, final String maximaCalculation, final String username) {
        var clearedUserAnswer = userAnswer.replace(";", "").replace(" ", "");

        boolean userInputIllegal = false;
        boolean maximaCalculationIllegal = false;
        if ((userInputIllegal = isInputIllegal(userAnswer, true, false))
                || (maximaCalculationIllegal = isInputIllegal(maximaCalculation, true, false))) {

        }

        var input = String.format("is(equal(%s,%s));", clearedUserAnswer, maximaCalculation);
        logger.info("User wants to validate his answer: {} to calculation: {}", clearedUserAnswer, maximaCalculation);

        String result;
        if ((result = sendToMaximaBackend(new MaximaQueryParams(input))) == null) {
            var ret = ResultDto.failure();
            ret.setFeedback("Maxima was not able to assess your answer");
            return ret;
        }
        return result.contains("true") ? ResultDto.success() : ResultDto.failure();
    }

    public CalculationResponse calculate(final CalculationRequest calculationRequest) {

        var inputBuilder = new StringBuilder();
        calculationRequest.getInputs().forEach(input -> {
            var calc = input.getMaximaExpression();
            var fixedCalculation = (calc.endsWith(";"))
                    ? calc
                    : calc.concat(";");
            inputBuilder.append(input.getVariableName()).append(":").append(fixedCalculation).append("\n");
        });
        String output;
        if ((output = sendToMaximaBackend(new MaximaQueryParams(inputBuilder.toString()))) == null) {
            logger.info("Could not calculate");
        }
        return parseMaximaOutput(output,calculationRequest);
    }

    private boolean isInputIllegal(final String input, boolean extremlyStrict, boolean allowMultiCharacterVariables) {

        var noLispMatcher = Pattern.compile(":?lisp").matcher(input);
        var forbiddenInputMatcher = Pattern.compile("(.{2,})").matcher(input);
        var functionCallMatcher = Pattern.compile("(.)").matcher(input);

        if (noLispMatcher.find()) {
            logger.info("User entered a lisp command! Bad!");
            return true;
        }

        if (forbiddenInputMatcher.find()) {
            logger.info("User tried to either enter a forbidden maxima function or entered multi character variable! Bad!");
            return true;
        }

        return false;

    }

    private CalculationResponse parseMaximaOutput(final String output, final CalculationRequest req) {

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
                var trueId = outputMatcher.group(2);
                outputLines.put(trueId, outputExpression);
                String expression = null;
                while (expressionMatcher.find()) {
                    logger.info(expressionMatcher.group(2));
                }
            }
        }

        var response = new CalculationResponse();
        req.getOutputVariableNames().forEach(variableName -> {
            int inputSize = req.getInputs().size();
            var inputObj = req.getInputs().stream().filter(input -> input.getVariableName().equals(variableName)).findFirst().orElseThrow();
            int idx = req.getInputs().indexOf(inputObj);
            var outputId = String.valueOf(idx + 9);

            var tuple = new MaximaTuple();
            tuple.setVariableName(variableName);
            tuple.setMaximaExpression(outputLines.get(outputId));
            response.addOutputTuple(tuple);
        });
        return response;
    }

    private String sendToMaximaBackend(@Valid final MaximaQueryParams params) {
        try {
            var builder = UriComponentsBuilder.fromUri(maximaUri)
                    .queryParam("input", UriUtils.encode(params.input, StandardCharsets.UTF_8))
                    .queryParam("timeout", params.timeout);
            if (params.version != null)
                builder.queryParam("version", UriUtils.encode(params.version, StandardCharsets.UTF_8));
            if (params.plotUrlBase != null) {
                builder.queryParam("plotUrlBase", UriUtils.encode(params.plotUrlBase, StandardCharsets.UTF_8));
            }

            var request = http.createRequest(
                    builder.build(true).toUri(),
                    HttpMethod.GET);
            var response = request.execute();
            logger.info("Sending {} to maxima backend", request.getURI().toString());
            return new String(response.getBody().readAllBytes());

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean checkBackendHealth() {
        try {
            var uri = UriComponentsBuilder.fromUri(maximaUri)
                    .query(maximaProps.getHealthcheckUrl())
                    .encode(StandardCharsets.UTF_8).build().toUri();
            var res = http.createRequest(uri, HttpMethod.GET).execute();
            return res.getStatusCode().is2xxSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
