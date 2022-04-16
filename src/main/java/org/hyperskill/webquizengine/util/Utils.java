package org.hyperskill.webquizengine.util;

import org.hyperskill.webquizengine.dto.*;
import org.hyperskill.webquizengine.exception.IllegalInputExpressionException;
import org.hyperskill.webquizengine.exception.InvalidAnswerOptions;
import org.hyperskill.webquizengine.model.Completion;
import org.hyperskill.webquizengine.model.Option;
import org.hyperskill.webquizengine.model.Quiz;
import org.hyperskill.webquizengine.model.RandomizedQuestion;
import org.hyperskill.webquizengine.service.MaximaQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    private Utils() { }

    public static void checkAnswerOptions(QuizDto quiz) {
        int numberOfOptionsInQuiz = quiz.getOptions().size();
        quiz.getAnswer().forEach(answerOptionIdx -> {
            if (answerOptionIdx < 0 || answerOptionIdx >= numberOfOptionsInQuiz) {
                throw new InvalidAnswerOptions();
            }
        });
    }

    public static Quiz convertQuizDtoToEntity(QuizDto quizDto) {
        var quiz = new Quiz();
        quiz.setId(quizDto.getId());
        quiz.setTitle(quizDto.getTitle());
        quiz.setText(quizDto.getText());

        var options = new ArrayList<Option>();
        for (var i = 0; i < quizDto.getOptions().size(); i++) {
            var option = new Option();
            option.setText(quizDto.getOptions().get(i));
            option.setCorrect(quizDto.getAnswer().contains(i));
            option.setPosition(i);
            options.add(option);
        }

        quiz.setOptions(options);

        return quiz;
    }

    public static RandomizedQuestionDto convertRandomQuestionToDto(RandomizedQuestion question) {
        var randomDto = new RandomizedQuestionDto();
        randomDto.setTemplateQuestionId(question.getTemplateQuestion().getId());
        randomDto.setTemplateQuestionCategory(question.getTemplateQuestion().getCategory());
        randomDto.setDescription(question.getDescription());
        randomDto.setId(question.getId());
        randomDto.setSeed(question.getSeed());

        question.getTemplateQuestion().getAnswers().forEach(answer -> {
            var answerDto = new AnswerDto();
            answerDto.setName(answer.getName());
            answerDto.setPrompt(answer.getPrompt());
            answerDto.setId(answer.getId());
            randomDto.addTemplateQuestionAnswer(answerDto);
        });
        return randomDto;
    }

    public static QuizDto convertQuizEntityToDtoWithoutAnswer(Quiz quiz) {
        var quizDto = new QuizDto();
        quizDto.setId(quiz.getId());
        quizDto.setTitle(quiz.getTitle());
        quizDto.setText(quiz.getText());
        quizDto.setOptions(quiz.getOptions().stream()
                .map(Option::getText)
                .collect(Collectors.toList()));
        return quizDto;
    }

    public static Set<Integer> getCorrectOptionsIndexes(List<Option> options) {
        var indexes = new HashSet<Integer>();
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getCorrect()) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public static CompletionDto convertCompletionEntityToDto(Completion completion) {
        var completionDto = new CompletionDto();
        completionDto.setQuizId(completion.getQuiz().getId());
        completionDto.setQuizTitle(completion.getQuiz().getTitle());
        completionDto.setCompletedAt(completion.getCompletedAt());
        return completionDto;
    }

    public static String createCalculationString(final ArrayList<MaximaExpressionTuple> calculations) {
        var sb = new StringBuilder();
        calculations.forEach(tuple -> {
            var cleanedExpression = tuple.getMaximaExpression().replace(";", "").replace(" ", "");
            var cleanedVariableName = tuple.getVariableName().replace(";", "").replace(" ", "");
            checkForIllegalInput(cleanedExpression, false, true, null);
            checkForIllegalInput(cleanedVariableName, false, true, null);
            var isFunction = Pattern.compile(".+\\(.+\\)").matcher(cleanedVariableName).find();
            sb.append(cleanedVariableName).append((isFunction) ? ":=" : ":").append(cleanedExpression).append(";");
        });
        return sb.toString();
    }


    public static void checkForIllegalInput(final String input, boolean noFunctions, boolean allowMultiCharacterVariables, final List<String> allowedFunctions) {

        var noColonMatcher = Pattern.compile(":").matcher(input);
        if (noColonMatcher.find()) {
            logger.info("User entered a :! Bad!");
            throw new IllegalInputExpressionException(String.format("Ausdruck %s beinhaltet verbotenes :", input));
        }

        var noLispMatcher = Pattern.compile(":?lisp").matcher(input);
        if (noLispMatcher.find()) {
            logger.info("User entered a lisp command! Bad!");
            throw new IllegalInputExpressionException("Lisp is forbidden as maxima input");
        }

        if (noFunctions) {
            var functionCallMatcher = Pattern.compile("(.+)\\(").matcher(input);
            if (functionCallMatcher.find()) {
                var functionName = functionCallMatcher.group(1);
                if (!allowedFunctions.contains(functionName)) {
                    logger.info("User tried to enter function {} or forgot to enter multiplication sign! Bad!", functionName);
                    throw new IllegalInputExpressionException(String.format("Function call %s is illegal! Check if you forgot to enter * for multiplication!", functionName));
                }
            }
        }
        var forbiddenMutliCharMatcher = Pattern.compile("(.{2,})").matcher(input);
        if (!allowMultiCharacterVariables) {
            if (forbiddenMutliCharMatcher.find()) {
                var multiChar = forbiddenMutliCharMatcher.group(1);
                logger.info("User entered multichar variable {} ", multiChar);
                throw new IllegalInputExpressionException(String.format("Multi characters are forbidden: %s", multiChar));
            }
        }
    }
}
