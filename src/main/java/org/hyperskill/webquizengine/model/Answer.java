package org.hyperskill.webquizengine.model;


import javax.persistence.*;

@Entity
@Table(name = "answer")
public class Answer {

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

    public AnswerTestType getType() {
        return type;
    }

    public void setType(AnswerTestType type) {
        this.type = type;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "answer_type_id")
    private AnswerTestType type;
}
