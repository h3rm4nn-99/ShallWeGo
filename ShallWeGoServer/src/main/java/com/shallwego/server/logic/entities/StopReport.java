package com.shallwego.server.logic.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Entity
public class StopReport extends Report implements Serializable {
    public StopReport() {}

    @OneToOne
    private Stop stopReported;

    public Stop getStopReported() {
        return stopReported;
    }

    public void setStopReported(Stop stopReported) {
        this.stopReported = stopReported;
    }
}
