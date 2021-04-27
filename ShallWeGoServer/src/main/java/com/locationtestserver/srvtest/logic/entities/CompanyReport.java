package com.locationtestserver.srvtest.logic.entities;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Entity
public class CompanyReport extends Report implements Serializable {

    @OneToOne
    private Company company;
}
