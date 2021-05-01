package com.locationtestserver.srvtest.logic.entities;

import javax.persistence.*;
import java.util.Date;

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
