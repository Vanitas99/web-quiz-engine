package org.hyperskill.webquizengine.model;


import javax.persistence.*;

@Entity
@Table(name = "answer")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column()
    private String prompt;


    /*
        A question-creator defined Maxima function, that returns [validity,correctness,feedback,""] or true/false
        indicating whether the user input answer is correct. This function is sequential, meaning that for the correctness of
        the answer needed property check should be ordered by their significance to the answer validation.
        E.g checkAnswerA(USER_ANSWER,CORRECT_ANSWER) := block(
            if (not matrixp(USER_ANSWER)) return false[]
           )
    */
    @Column(nullable = true)
    private String assessmentFunction;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getAssessmentFunction() {
        return assessmentFunction;
    }

    public void setAssessmentFunction(String assessmentFunction) {
        this.assessmentFunction = assessmentFunction;
    }

}
