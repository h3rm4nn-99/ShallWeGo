package com.locationtestserver.srvtest.logic.entities;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
public class Line implements Serializable {

    private String identifier;
    private String firstTerminus;
    private String secondTerminus;

    @ManyToOne
    private Company company;
}
