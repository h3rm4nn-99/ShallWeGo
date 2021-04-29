package com.locationtestserver.srvtest.logic.entities;

import com.locationtestserver.srvtest.logic.service.LineCompositeKey;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(LineCompositeKey.class)
public class Line implements Serializable {

    public Line() {}

    @Id
    private String identifier;

    @ManyToOne
    @Id
    private Company company;

    private String firstTerminus;
    private String secondTerminus;

    @OneToOne
    private Report lineReport;

}
