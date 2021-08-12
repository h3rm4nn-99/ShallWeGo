package com.shallwego.server.logic.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
public class DestinationsByReportAndLine implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    private Line targetLine;

    @ElementCollection
    private Set<String> targetDestinations = new HashSet<>();

    @ManyToOne
    private TemporaryEventReport targetReport;

    public void setTargetDestinations(Set<String> targetDestinations) {
        this.targetDestinations = targetDestinations;
    }

    public DestinationsByReportAndLine() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Line getTargetLine() {
        return targetLine;
    }

    public void setTargetLine(Line targetLine) {
        this.targetLine = targetLine;
    }

    public Set<String> getTargetDestinations() {
        return targetDestinations;
    }

    public TemporaryEventReport getTargetReport() {
        return targetReport;
    }

    public void setTargetReport(TemporaryEventReport targetReport) {
        this.targetReport = targetReport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DestinationsByReportAndLine)) return false;
        DestinationsByReportAndLine that = (DestinationsByReportAndLine) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getTargetLine(), that.getTargetLine()) && Objects.equals(getTargetDestinations(), that.getTargetDestinations()) && Objects.equals(getTargetReport(), that.getTargetReport());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTargetLine(), getTargetDestinations(), getTargetReport());
    }

    @Override
    public String toString() {
        return "DestinationsByReportAndLine{" +
                "id=" + id +
                ", targetLine=" + targetLine +
                ", targetDestinations=" + targetDestinations +
                ", targetReport=" + targetReport +
                '}';
    }
}
