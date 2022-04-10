package org.hyperskill.webquizengine.controller;

import org.hyperskill.webquizengine.dto.CalculationRequest;
import org.hyperskill.webquizengine.dto.CalculationResponse;
import org.hyperskill.webquizengine.dto.MaximaQuestionDto;
import org.hyperskill.webquizengine.dto.ResultDto;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.hyperskill.webquizengine.service.MaximaQuestionService;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;

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

    @PostMapping(path = "/question" ,produces = APPLICATION_JSON_VALUE)
    public Long create(@RequestBody @Valid MaximaQuestionDto question,
                       @Autowired Principal principal) {
        logger.info("User {} wants to create a quiz", principal.getName());
        return service.createQuestion(question, principal.getName());
    }

    @PostMapping(path = "/assess", produces = APPLICATION_JSON_VALUE)
    public ResultDto assessAnswer(@RequestParam String correctAnswer,
                                  @RequestParam String userAnswer,
                                  @Autowired Principal principal,
                                  UriComponentsBuilder builder) {
        return service.assessUserAnswer(userAnswer, correctAnswer, principal.getName());
    }

    @DeleteMapping(path = "/question/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteMaximaQuestion(@PathVariable long id,
                           @Autowired Principal principal) {
        logger.info("User {} wants to delete a quiz with id {}", principal.getName(), id);
        service.deleteQuestion(id, principal.getName());
    }

    @GetMapping(path = "/question/{id}", produces = APPLICATION_JSON_VALUE)
    public MaximaQuestionDto getQuestionById(@PathVariable long id,
                                             @Autowired Principal principal) {
        return new ModelMapper().map(service.getQuestionById(id), MaximaQuestionDto.class);
    }

    @GetMapping(path = "/questions/{category}", produces = APPLICATION_JSON_VALUE)
    public Slice<MaximaQuestionDto> getQuestionsByCategory(@PathVariable String category,
                                                           @RequestParam int page,
                                                           @RequestParam int size,
                                                           @Autowired Principal principal) {
       return service.findQuestionsByCategory(category, page, size).map(question ->
                new ModelMapper().map(question, MaximaQuestionDto.class)
        );
    }

    @GetMapping("/plot/{id}")
    public ResponseEntity<Resource> getPlotForFunction() throws IOException {
        String inputFile = "/path/to/image.svg";
        Path path = new File(inputFile).toPath();
        FileSystemResource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Files.probeContentType(path)))
                .body(resource);
    }

    @PostMapping(path = "/calculate", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CalculationResponse calculate(@Valid @RequestBody CalculationRequest calc) {
        return service.calculate(calc);
    }

    @GetMapping(path = "/health")
    public boolean healthCheckMaxima() {
        return service.checkBackendHealth();
    }
}
