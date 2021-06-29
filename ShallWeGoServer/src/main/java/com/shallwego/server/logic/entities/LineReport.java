package com.shallwego.server.logic.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Entity
public class LineReport extends Report implements Serializable {

    public LineReport() {
        super();
    }

    @OneToOne(cascade = CascadeType.ALL)
    private Line lineAffected;

    public Line getLineAffected() {
        return lineAffected;
    }

    public void setLineAffected(Line lineAffected) {
        this.lineAffected = lineAffected;
    }
}
