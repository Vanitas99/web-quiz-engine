package org.hyperskill.webquizengine.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "maxima_question")
public class MaximaQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String expressions;

    @Column(nullable = false)
    private Long numberOfExpressions;

    @Column()
    private String category;

    @Column
    private boolean globalSimplification;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable=false)
    private User createdBy;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "answer_id", nullable = false)
    private List<Answer> answers = new ArrayList<Answer>();

    public boolean isGlobalSimplification() {
        return globalSimplification;
    }

    public void setGlobalSimplification(boolean globalSimplification) {
        this.globalSimplification = globalSimplification;
    }

    public Long getNumberOfExpressions() {
        return numberOfExpressions;
    }

    public void setNumberOfExpressions(Long numberOfExpressions) {
        this.numberOfExpressions = numberOfExpressions;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public Answer getAnswerByName(final String name) {
        return answers.stream().filter(answer -> {
            return Objects.equals(answer.getName(), name);
        }).findAny().orElseThrow(IllegalArgumentException::new);
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public void addAnswer(Answer answer) {
        this.answers.add(answer);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getExpressions() {
        return expressions;
    }

    public void setExpressions(String expressions) {
        this.expressions = expressions;
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
}
