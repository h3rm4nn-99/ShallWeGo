package com.locationtestserver.srvtest.logic.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.List;

@Entity
public class Company implements Serializable {
    @Id
    private String nome;

    @OneToMany(mappedBy = "company")
    private List<Line> linee;
}
