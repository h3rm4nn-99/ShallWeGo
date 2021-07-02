package com.shallwego.server.logic.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Stop {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Line> lines;

    @OneToOne(mappedBy = "stopReported")
    private StopReport stopReport;

    private Boolean hasShelter;
    private Boolean hasTimeTables;
    private Boolean hasStopSign;

    private Double latitude;
    private Double longitude;

    public Stop() {}

    public static Stop newInstance() {
        Stop s = new Stop();
        s.lines = new ArrayList<>();
        return s;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public Boolean getHasShelter() {
        return hasShelter;
    }

    public void setHasShelter(Boolean hasShelter) {
        this.hasShelter = hasShelter;
    }

    public Boolean getHasTimeTables() {
        return hasTimeTables;
    }

    public void setHasTimeTables(Boolean hasTimeTables) {
        this.hasTimeTables = hasTimeTables;
    }

    public Boolean getHasStopSign() {
        return hasStopSign;
    }

    public void setHasStopSign(Boolean hasStopSign) {
        this.hasStopSign = hasStopSign;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Report getStopReport() {
        return stopReport;
    }

    public void setStopReport(StopReport stopReport) {
        this.stopReport = stopReport;
    }

    public void addLine(Line l) {
        lines.add(l);
    }
}
