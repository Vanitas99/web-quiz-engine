package org.hyperskill.webquizengine.service;

import org.hyperskill.webquizengine.dto.*;
import org.hyperskill.webquizengine.exception.*;
import org.hyperskill.webquizengine.model.Answer;
import org.hyperskill.webquizengine.model.MaximaQuestion;
import org.hyperskill.webquizengine.model.RandomizedQuestion;
import org.hyperskill.webquizengine.repository.RandomizedQuestionRepository;
import org.hyperskill.webquizengine.repository.MaximaQuestionRepository;
import org.hyperskill.webquizengine.repository.UserRepository;
import org.hyperskill.webquizengine.util.BackendProperties;
import org.hyperskill.webquizengine.util.MaximaClient;
import org.hyperskill.webquizengine.util.Utils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.validation.constraints.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MaximaQuestionService {

    public static List<String> allowedUserFunctions = List.of("sin", "cos");

    private final MaximaQuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final BackendProperties maximaProps;
    private final Logger logger = LoggerFactory.getLogger(MaximaQuestionService.class);
    private final RandomizedQuestionRepository randomRepository;
    private final MaximaClient maxima;


    @Autowired
    public MaximaQuestionService(MaximaQuestionRepository repository,
                                 UserRepository userRepository,
                                 BackendProperties maximaProps, RandomizedQuestionRepository answerTypeRepository) {
        this.questionRepository = repository;
        this.userRepository = userRepository;
        this.maximaProps = maximaProps;
        this.randomRepository = answerTypeRepository;
        this.maxima = new MaximaClient(this.maximaProps);
    }

    public long createTemplatedQuestion(final MaximaQuestionTemplate dto, final String username) {
        var user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        var question = new MaximaQuestion();
        var calculationString = Utils.createCalculationString(dto.getCalculations());
        logger.info("User {} wants to create a new Question with calculation {}", username, calculationString);
        question.setExpressions(calculationString);
        question.setCreatedBy(user);
        question.setNumberOfExpressions((long) dto.getCalculations().size());
        question.setDescription(dto.getDescription());
        dto.getAnswers().forEach(answer -> {
            question.addAnswer(new ModelMapper().map(answer, Answer.class));
        });
        return questionRepository.save(question).getId();
    }

    public MaximaQuestionTemplate getTemplatedQuestion(final long id, final String username) {
        var user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        var question = questionRepository.findById(id).orElseThrow(QuizNotFoundException::new);
        var questionDto = new MaximaQuestionTemplate();

        question.getAnswers().forEach(answer -> {
            questionDto.addAnswer(new ModelMapper().map(answer, AnswerDto.class));
        });
        var allExpressions = question.getExpressions();
        var calculations = new ArrayList<MaximaExpressionTuple>();
        var tmp = Arrays.stream(allExpressions.split(";")).collect(Collectors.toList());
        var pattern = Pattern.compile("(.*):=?(.*)");
        tmp.forEach(expressionTuple -> {
            var matcher = pattern.matcher(expressionTuple);
            if (matcher.find()) {
                var variable = matcher.group(1);
                var expression = matcher.group(2);
                var tuple = new MaximaExpressionTuple();
                tuple.setMaximaExpression(expression);
                tuple.setVariableName(variable);
                calculations.add(tuple);
            }
        });
        questionDto.setCalculations(calculations);
        questionDto.setDescription(question.getDescription());
        questionDto.setId(question.getId());
        questionDto.setCategory(question.getCategory());
        return questionDto;
    }

    public void deleteQuestionTemplate(@NotNull final long id, final String username) {
        var user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        var question = questionRepository.findById(id).orElseThrow(QuizNotFoundException::new);
        if (user.getId().equals(question.getCreatedBy().getId())) {
            try {
                questionRepository.deleteById(id);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                throw new QuizNotFoundException();
            }
        }
        throw new NotAuthorizedException("You are not allowed to delete that question");
    }

    public RandomizedQuestionDto getRandomizedQuestionById(final long id, final String username) {
        var question = randomRepository.findById(id).orElseThrow(QuizNotFoundException::new);
        return Utils.convertRandomQuestionToDto(question);
    }

    public RandomizedQuestionDto createRandomizedQuestionById(final long id, final long seed, final String username) {
        var user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        var questionTemplate = questionRepository.findById(id).orElseThrow(QuizNotFoundException::new);

        var randomSeed = (seed == -1) ? System.currentTimeMillis() & 0xffff : seed;

        var props = new MaximaClient.CalculationProperties(
                questionTemplate.getExpressions(),
                questionTemplate.getNumberOfExpressions(),
                randomSeed,
                questionTemplate.isGlobalSimplification());
        var questionDescription = maxima.getTexOutputs(props, questionTemplate.getDescription(), false);

        logger.info("Got new tex description with text : {}", questionDescription);

        var randomized = new RandomizedQuestion();
        randomized.setId(id);
        randomized.setSeed(randomSeed);
        randomized.setDescription(questionDescription);
        randomized.setTemplateQuestion(questionTemplate);
        randomized.setCreatedBy(user);
        randomized.setSeed(randomSeed);
        randomRepository.save(randomized);

        return Utils.convertRandomQuestionToDto(randomized);
    }

    public Slice<MaximaQuestion> findQuestionsByCategory(final String category, final int page, final int size) {
        var pageable = PageRequest.of(page, size, Sort.by("id"));
        return questionRepository.findQuestionsByCategory(category, pageable);
    }

    public ArrayList<MaximaResult> assessAnswers(final ArrayList<MaximaExpressionTuple> userAnswers, final Long questionId, final String username) {

        var user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        var clearedUserAnswers = new ArrayList<MaximaExpressionTuple>();

        var question = randomRepository.findById(questionId).orElseThrow(QuizNotFoundException::new);
        var templateAnswers = question.getTemplateQuestion().getAnswers();

        var props = new MaximaClient.CalculationProperties(
                question.getTemplateQuestion().getExpressions(),
                question.getTemplateQuestion().getNumberOfExpressions(),
                question.getSeed(),
                question.getTemplateQuestion().isGlobalSimplification());

        var results = new ArrayList<MaximaResult>();
        templateAnswers.forEach(correctAnswer -> {
            var answerVariableName = correctAnswer.getName();
            var result = new MaximaResult();
            var userAnswerTuple = userAnswers.stream().filter(answer -> {
                return Objects.equals(answer.getVariableName(), answerVariableName);
            }).findFirst();
            logger.info("{}", userAnswerTuple);
            if (userAnswerTuple.isEmpty()) {
                result.setCorrect(false);
                result.setAnswerName(answerVariableName);
                result.setFeedback("User forgot to input anything!");
                results.add(result);
                return;
            }

            var userAnswer = userAnswerTuple.get().getMaximaExpression();
            var assessmentFunction = correctAnswer.getAssessmentFunction();
            if (assessmentFunction == null) {
                assessmentFunction = "return(ATAlgEquiv(USER_ANSWER,CORRECT_ANSWER))";
            }
            results.add(maxima.assesUserInput(props, userAnswer, answerVariableName, assessmentFunction));
        });

        return results;
    }

    public CalculationResponse calculate(final CalculationRequest request, final long seed) {

        var expressions = request.getExpressions();
        var calculationString = Utils.createCalculationString(expressions);
        var props = new MaximaClient.CalculationProperties(
                calculationString,
                expressions.size(),
                (seed == -1) ? System.currentTimeMillis() & 0xffffffff : seed,
                request.isGlobalSimp());
        return maxima.calculate(props,request.getOutputVariables());
    }

    public boolean checkBackendHealth() {
        return maxima.checkBackendHealth();
    }

}
