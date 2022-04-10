package org.hyperskill.webquizengine.dto;

import org.hyperskill.webquizengine.model.RandomVariableDto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class MaximaQuestionDto implements Serializable {

    private Long id;
    @NotBlank(message = "Description must be set for a question")
    private String description;
    @Valid
    private CalculationRequest calculations;

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRandomizedVars(List<RandomVariableDto> randomizedVars) {
        this.randomizedVars = randomizedVars;
    }

    private List<RandomVariableDto> randomizedVars;

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public List<RandomVariableDto> getRandomizedVars() {
        return randomizedVars;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaximaQuestionDto entity = (MaximaQuestionDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.description, entity.description) &&
                Objects.equals(this.randomizedVars, entity.randomizedVars);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, randomizedVars);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "description = " + description + ", " +
                "randomizableVars = " + randomizedVars + ")";
    }
}
