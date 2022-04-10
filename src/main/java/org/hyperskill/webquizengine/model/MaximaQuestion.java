package org.hyperskill.webquizengine.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Entity
@Table(name = "maxima")
public class MaximaQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String equation;

    @Column()
    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }



    public String getEquation() {
        return equation;
    }

    public void setEquation(String equation) {
        this.equation = equation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RandomVariable> getRandomizableVars() {
        return randomizableVars;
    }

    public void setRandomizableVars(List<RandomVariable> randomizableVars) {
        this.randomizableVars = randomizableVars;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "var_id")
    private List<RandomVariable> randomizableVars = new ArrayList<>();

}
