package com.shallwego.server.logic.entities;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Entity
public class LineReport extends Report implements Serializable {

    public LineReport() {
        super();
    }

    @OneToOne
    private Line lineAffected;
}
