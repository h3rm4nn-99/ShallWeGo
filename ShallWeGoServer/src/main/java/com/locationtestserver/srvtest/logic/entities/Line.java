package com.locationtestserver.srvtest.logic.entities;

import com.locationtestserver.srvtest.logic.service.LineCompositeKey;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Entity
@IdClass(LineCompositeKey.class)
public class Line implements Serializable {

    @Id
    private String identifier;

    @ManyToOne
    @Id
    private Company company;

    private String firstTerminus;
    private String secondTerminus;

}
