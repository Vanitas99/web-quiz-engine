package org.hyperskill.webquizengine.model;

import javax.persistence.*;

@Entity
@Table(name = "answer_type")
public class AnswerTestType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSolveEquation() {
        return solveEquation;
    }

    public void setSolveEquation(String solveEquation) {
        this.solveEquation = solveEquation;
    }

    @Column
    private String solveEquation;
}
