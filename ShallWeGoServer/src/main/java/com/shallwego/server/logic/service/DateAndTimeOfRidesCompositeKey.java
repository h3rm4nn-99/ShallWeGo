package com.shallwego.server.logic.service;


import com.shallwego.server.logic.entities.Line;
import com.shallwego.server.logic.entities.Stop;

import java.io.Serializable;
import java.util.Objects;

public class DateAndTimeOfRidesCompositeKey implements Serializable {
    private Stop targetStop;
    private Line targetLine;

    public DateAndTimeOfRidesCompositeKey() {}

    public Stop getTargetStop() {
        return targetStop;
    }

    public void setTargetStop(Stop targetStop) {
        this.targetStop = targetStop;
    }

    public Line getTargetLine() {
        return targetLine;
    }

    public void setTargetLine(Line targetLine) {
        this.targetLine = targetLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateAndTimeOfRidesCompositeKey that = (DateAndTimeOfRidesCompositeKey) o;
        return Objects.equals(targetStop, that.targetStop) && Objects.equals(targetLine, that.targetLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetStop, targetLine);
    }

    @Override
    public String toString() {
        return "DateAndTimeOfRidesCompositeKey{" +
                "targetStop=" + targetStop +
                ", targetLine=" + targetLine +
                '}';
    }
}
