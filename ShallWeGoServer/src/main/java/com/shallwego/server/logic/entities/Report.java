package com.shallwego.server.logic.entities;

import javax.persistence.*;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Report {

    public Report() {}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String date;

    @ManyToOne
    private User user;

    private boolean verified;

    @ManyToMany(mappedBy = "assignedTo")
    private List<User> verifiers;

    public List<User> getVerifiers() {
        return verifiers;
    }

    public void addVerifier(User verifier) {
        verifiers.add(verifier);
    }

}
