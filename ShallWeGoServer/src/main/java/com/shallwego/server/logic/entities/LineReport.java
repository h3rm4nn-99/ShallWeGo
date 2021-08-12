package com.shallwego.server.logic.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.Objects;

@Entity
public class LineReport extends Report implements Serializable {

    public LineReport() {
        super();
    }

    @OneToOne
    private Line lineAffected;

    public Line getLineAffected() {
        return lineAffected;
    }

    public void setLineAffected(Line lineAffected) {
        this.lineAffected = lineAffected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineReport)) return false;
        if (!super.equals(o)) return false;
        LineReport that = (LineReport) o;
        return Objects.equals(getLineAffected(), that.getLineAffected());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getLineAffected());
    }
}
