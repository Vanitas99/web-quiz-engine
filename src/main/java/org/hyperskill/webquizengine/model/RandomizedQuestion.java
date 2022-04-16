package org.hyperskill.webquizengine.model;

import javax.persistence.*;

@Entity
@Table(name = "randomized_question")
public class RandomizedQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String description;

    @OneToOne()
    @JoinColumn(name = "template_id")
    private MaximaQuestion templateQuestion;

    @Column(nullable = false)
    private long seed;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User createdBy;

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MaximaQuestion getTemplateQuestion() {
        return templateQuestion;
    }

    public void setTemplateQuestion(MaximaQuestion templateQuestion) {
        this.templateQuestion = templateQuestion;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}