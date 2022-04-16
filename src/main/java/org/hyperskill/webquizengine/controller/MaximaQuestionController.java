package org.hyperskill.webquizengine.controller;

import org.hyperskill.webquizengine.dto.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.hyperskill.webquizengine.service.MaximaQuestionService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/maxima")
public class MaximaQuestionController {

    private final Logger logger = LoggerFactory.getLogger(MaximaQuestionController.class);
    private final MaximaQuestionService service;

    @Autowired
    public MaximaQuestionController(MaximaQuestionService service) {
        this.service = service;
    }

    @GetMapping
    public void root() {

    }

    @PostMapping(path = "/question" ,produces = APPLICATION_JSON_VALUE)
    public Long create(@RequestBody @Valid MaximaQuestionTemplate question,
                       @Autowired Principal principal) {
        logger.info("User {} wants to create a quiz", principal.getName());
        return service.createTemplatedQuestion(question, principal.getName());
    }

    @DeleteMapping(path = "/question/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteMaximaQuestion(@PathVariable long id,
                           @Autowired Principal principal) {
        logger.info("User {} wants to delete a quiz with id {}", principal.getName(), id);
        service.deleteQuestionTemplate(id, principal.getName());
    }

    @GetMapping(path = "/question/{id}", produces = APPLICATION_JSON_VALUE)
    public MaximaQuestionTemplate getQuestionById(@PathVariable long id,
                                                  @Autowired Principal principal) {
        return service.getTemplatedQuestion(id, principal.getName());
    }

    @GetMapping(path = "/questions/{category}", produces = APPLICATION_JSON_VALUE)
    public Slice<MaximaQuestionTemplate> getQuestionsByCategory(@PathVariable String category,
                                                                @RequestParam int page,
                                                                @RequestParam int size,
                                                                @Autowired Principal principal) {
       return service.findQuestionsByCategory(category, page, size).map(question ->
                new ModelMapper().map(question, MaximaQuestionTemplate.class)
        );
    }

    @PostMapping(path = "/calculate")
    public CalculationResponse calculateRequest(@RequestBody CalculationRequest request,
                                                @RequestParam String seed,
                                                @Autowired Principal principal) {
        return service.calculate(request, Long.parseLong(seed));
    }

    @GetMapping(path = "/question/{id}/randomize")
    public RandomizedQuestionDto randomizeNewQuestion(@PathVariable long id, @Autowired Principal principal) {
        return service.createRandomizedQuestionById(id, -1, principal.getName());
    }

    @PostMapping(path = "/randomizedQuestion/{id}")
    public ArrayList<MaximaResult> solveRandomizedQuestion(@PathVariable long id,
                                             @RequestBody ArrayList<MaximaExpressionTuple> answers,
                                             @Autowired Principal principal) {
        return service.assessAnswers(answers,id, principal.getName());
    }

    @GetMapping(path = "/randomizedQuestion/{id}")
    public RandomizedQuestionDto getRandomizedQuestion(@PathVariable long id, @Autowired Principal principal) {
        return service.getRandomizedQuestionById(id, principal.getName());
    }

    @GetMapping(path = "/health")
    public boolean healthCheckMaxima() {
        return service.checkBackendHealth();
    }
}
