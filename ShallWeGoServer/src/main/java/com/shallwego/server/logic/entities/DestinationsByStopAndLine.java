package com.shallwego.server.logic.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class DestinationsByStopAndLine implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    private Line targetLine;

    @ElementCollection
    private List<String> targetDestinations = new ArrayList<>();

    @ManyToOne
    private Stop targetStop;

    public DestinationsByStopAndLine() {}

    public DestinationsByStopAndLine(Line targetLine, Stop targetStop) {
        this.targetLine = targetLine;
        this.targetStop = targetStop;
    }

    public Line getTargetLine() {
        return targetLine;
    }

    public void setTargetLine(Line targetLine) {
        this.targetLine = targetLine;
    }

    public List<String> getTargetDestinations() {
        return targetDestinations;
    }

    public void setTargetDestinations(List<String> targetDestinations) {
        this.targetDestinations = targetDestinations;
    }

    public Stop getTargetStop() {
        return targetStop;
    }

    public void setTargetStop(Stop targetStop) {
        this.targetStop = targetStop;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DestinationsByStopAndLine)) return false;
        DestinationsByStopAndLine that = (DestinationsByStopAndLine) o;
        return Objects.equals(getTargetLine(), that.getTargetLine()) && Objects.equals(getTargetDestinations(), that.getTargetDestinations()) && Objects.equals(getTargetStop(), that.getTargetStop());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTargetLine(), getTargetDestinations(), getTargetStop());
    }

    @Override
    public String toString() {
        return "DestinationsByStopAndLine{" +
                "targetLine=" + targetLine +
                ", targetDestinations=" + targetDestinations +
                ", targetStop=" + targetStop +
                '}';
    }
}
