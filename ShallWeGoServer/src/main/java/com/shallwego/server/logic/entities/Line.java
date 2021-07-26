package com.shallwego.server.logic.entities;

import com.shallwego.server.logic.service.LineCompositeKey;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@IdClass(LineCompositeKey.class)
public class Line implements Serializable {

    public Line() {}

    @Id
    @Column(length = 100)
    private String identifier;

    @ManyToOne
    @Id
    private Company company;

    private String origin;

    @Id
    @Column(length = 100)
    private String destination;

    @ManyToMany(mappedBy = "lines")
    private List<Stop> stops;

    @ManyToMany(mappedBy = "linesAffectedEvent")
    private List<TemporaryEventReport> eventReports;

    @OneToOne(mappedBy = "lineAffected")
    private LineReport lineReport;

    @OneToMany(mappedBy = "targetLine")
    private List<DateAndTimesOfRides> dateAndTimesOfRidesByLine;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL)
    private List<Route> paths;

    public List<Route> getPaths() {
        return paths;
    }

    public void setPaths(List<Route> paths) {
        this.paths = paths;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Report getLineReport() {
        return lineReport;
    }

    public void setLineReport(LineReport lineReport) {
        this.lineReport = lineReport;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    public List<TemporaryEventReport> getEventReports() {
        return eventReports;
    }

    public void setEventReports(List<TemporaryEventReport> eventReports) {
        this.eventReports = eventReports;
    }

    public List<DateAndTimesOfRides> getDateAndTimesOfRidesByLine() {
        return dateAndTimesOfRidesByLine;
    }

    public void setDateAndTimesOfRidesByLine(List<DateAndTimesOfRides> dateAndTimesOfRidesByLine) {
        this.dateAndTimesOfRidesByLine = dateAndTimesOfRidesByLine;
    }
}
