package com.locationtestserver.srvtest.logic.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.List;

@Entity
public class Company implements Serializable {
    @Id
    private String name;
    private String website;

    @OneToMany(mappedBy = "company")
    private List<Line> linee;

    @OneToOne
    private Report companyReport;
}
