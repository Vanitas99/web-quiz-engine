package org.hyperskill.webquizengine.model;

import java.io.Serializable;
import java.util.Objects;

public class RandomVariableDto implements Serializable {
    private final Long id;
    private final String name;
    private final RandomVariable.Sign signed;
    private final RandomVariable.Type type;

    public RandomVariableDto(Long id, String name, RandomVariable.Sign signed, RandomVariable.Type type) {
        this.id = id;
        this.name = name;
        this.signed = signed;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RandomVariable.Sign getSigned() {
        return signed;
    }

    public RandomVariable.Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomVariableDto entity = (RandomVariableDto) o;
        return Objects.equals(this.id, entity.id) &&
                Objects.equals(this.name, entity.name) &&
                Objects.equals(this.signed, entity.signed) &&
                Objects.equals(this.type, entity.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, signed, type);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "signed = " + signed + ", " +
                "type = " + type + ")";
    }
}
