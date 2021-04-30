package com.locationtestserver.srvtest.logic.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@DiscriminatorValue("LineReport")
public class LineReport extends Report implements Serializable {

    public LineReport() {
        super();
    }

    @OneToOne
    private Line lineAffected;
}
