package org.hyperskill.webquizengine.model;


import org.springframework.web.bind.annotation.MatrixVariable;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "randvar")
public class RandomVariable {

    public enum Type {
        COMPLEX,
        INTEGER,
        DECIMAL
    }

    public enum Sign {
        POSITVE,
        NEGATIVE,
        NOCONSTRAINT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sign signed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

}
