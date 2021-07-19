package com.shallwego.server.logic.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Report {

    public Report() {}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private Date date;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setVerifiers(List<User> verifiers) {
        this.verifiers = verifiers;
    }
}
