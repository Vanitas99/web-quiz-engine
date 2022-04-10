package org.hyperskill.webquizengine.service;

import org.hyperskill.webquizengine.dto.*;
import org.hyperskill.webquizengine.exception.*;
import org.hyperskill.webquizengine.model.Answer;
import org.hyperskill.webquizengine.model.AnswerTestType;
import org.hyperskill.webquizengine.model.MaximaQuestion;
import org.hyperskill.webquizengine.repository.AnswerTypeRepository;
import org.hyperskill.webquizengine.repository.MaximaQuestionRepository;
import org.hyperskill.webquizengine.repository.RandomVariableRepository;
import org.hyperskill.webquizengine.repository.UserRepository;
import org.hyperskill.webquizengine.util.BackendProperties;
import org.hyperskill.webquizengine.util.AnswersDto;
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

    public static List<String> allowedUserFunctions = List.of("sin", "cos");

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
    private final AnswerTypeRepository answerTypeRepository;


    @Autowired
    public MaximaQuestionService(MaximaQuestionRepository repository,
                                 RandomVariableRepository variableRepository,
                                 UserRepository userRepository,
                                 BackendProperties maximaProps, AnswerTypeRepository answerTypeRepository) {
        this.questionRepository = repository;
        this.variableRepository = variableRepository;
        this.userRepository = userRepository;
        this.maximaProps = maximaProps;

        maximaUri = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(maximaProps.getIp())
                .port(maximaProps.getPort())
                .replacePath("/maxima").build().toUri();
        this.answerTypeRepository = answerTypeRepository;
    }

    public long createQuestion(final MaximaQuestionCreationDto dto, final String username) {
        var user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        //var question = new ModelMapper().map(dto, MaximaQuestion.class);
        var question = new MaximaQuestion();
        StringBuilder storeEquations = new StringBuilder();
        dto.getCalculations().getInputs().forEach(maximaTuple -> {
            storeEquations.append(maximaTuple.getVariableName()).append(":").append(maximaTuple.getMaximaExpression()).append(";");
        });
        logger.info(storeEquations.toString());
        question.setExpressions(storeEquations.toString());
        question.setDescription(dto.getDescription());
        question.setNumberOfExpressions((long) dto.getCalculations().getInputs().size());
        dto.getAnswers().forEach(answerTestDto -> {
            question.addAnswer(new ModelMapper().map(answerTestDto, Answer.class));
        });
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

    public MaximaQuestionReturnDto getRandomizedQuestionById(final long id) {
        var randomSeed = System.currentTimeMillis() & 0xffff;
        var question = questionRepository.findById(id).orElseThrow(QuizNotFoundException::new);
        var calculations = question.getExpressions();

        MaximaQuestionReturnDto dto = new MaximaQuestionReturnDto();
        dto.setId(id);
        dto.setSeed(randomSeed);
        var answers = new ArrayList<AnswerTestDto>();
        question.getAnswers().forEach(answer -> {
            var answerTestDto = new AnswerTestDto();
            answerTestDto.setName(answer.getName());
            answerTestDto.setType(answer.getType());
            answers.add(answerTestDto);
        });
        dto.setAnswerVariables(question.getAnswers());
    }

    public Slice<MaximaQuestion> findQuestionsByCategory(final String category, final int page,  final int size) {
        var pageable = PageRequest.of(page, size, Sort.by("id"));
        return questionRepository.findQuestionsByCategory(category, pageable);
    }
    public ResultDto assessAnswer(final String userAnswer, final Long questionId, final Long seed, final String username, final String answerName) {

        var clearedUserAnswer = userAnswer.replace(";", "").replace(" ", "");
        isInputIllegal(clearedUserAnswer, true, false, allowedUserFunctions);
        var question = questionRepository.findById(questionId).orElseThrow(QuizNotFoundException::new);
        var solveEquation = question.getAnswerByName(answerName).getType().getSolveEquation();

        var prefix = new ArrayList<String>();
        prefix.add(String.format("stack_randseed(%d);", seed));

        var amountOfCalculations = question.getNumberOfExpressions();

        var postfix = new ArrayList<String>();
        postfix.add(String.format(solveEquation, userAnswer, answerName));

        var inputString = new StringBuilder()
                .append(prefix)
                .append(question.getExpressions())
                .append(postfix)
                .toString();
        var output = sendToMaximaBackend(new MaximaQueryParams(inputString));

        String result;
        if ((result = sendToMaximaBackend(new MaximaQueryParams(inputString))) == null) {
            var ret = ResultDto.failure();
            ret.setFeedback("Maxima was not able to assess your answer");
            return ret;
        }

        var t = parseMaximaOutput(result, );


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
        return parseMaximaOutput(output, calculationRequest, 9);
    }

    private void isInputIllegal(final String input, boolean noFunctionCalls, boolean allowMultiCharacterVariables, final List<String> allowedFunctions) throws IllegalColonException, IllegalFunctionException, IllegalMultiCharException{

        var noColonMatcher = Pattern.compile(":").matcher(input);
        if (noColonMatcher.find()) {
            logger.info("User entered a :! Bad!");
            throw new IllegalColonException();
        }

        var noLispMatcher = Pattern.compile(":?lisp").matcher(input);
        if (noLispMatcher.find()) {
            logger.info("User entered a lisp command! Bad!");
            throw new IllegalFunctionException();
        }

        if (noFunctionCalls) {
            var functionCallMatcher = Pattern.compile("(.+)\\(").matcher(input);
            if (functionCallMatcher.find()) {
                var functionName = functionCallMatcher.group(1);
                if (!allowedFunctions.contains(functionName)) {
                    logger.info("User tried to enter function {} or forgot to enter multiplication sign! Bad!", functionName);
                    throw new IllegalFunctionException();
                }
            }
        }
        var forbiddenMutliCharMatcher = Pattern.compile("(.{2,})").matcher(input);
        if (!allowMultiCharacterVariables) {
            if (forbiddenMutliCharMatcher.find()) {
                var multiChar = forbiddenMutliCharMatcher.group(1);
                logger.info("User entered multichar variable {} ", multiChar);
                throw new IllegalMultiCharException();
            }
        }
    }

    private CalculationResponse parseMaximaOutput(final String output, final CalculationRequest req, int startIdx) {

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
            var outputId = String.valueOf(idx + startIdx);

            var tuple = new AnswersDto();
            tuple.setVariableName(variableName);
            tuple.setMaximaExpression(outputLines.get(outputId));
            response.addOutputTuple(tuple);
        });
        return response;
    }

    private String sendToMaximaBackend(@Valid final MaximaQueryParams params) {
        logger.info("Sending {} to maxima", params.input);

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
