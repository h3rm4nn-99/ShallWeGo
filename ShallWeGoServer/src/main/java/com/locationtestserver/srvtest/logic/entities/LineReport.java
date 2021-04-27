package com.locationtestserver.srvtest.logic.entities;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Entity
public class LineReport extends Report implements Serializable {

    @OneToOne
    private Line lineAffected;
}
