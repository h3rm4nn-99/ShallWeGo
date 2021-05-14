package com.shallwego.server.logic.entities;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Report {

    public Report() {}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String date;

    @ManyToOne
    private User user;

    private boolean verified;
}
