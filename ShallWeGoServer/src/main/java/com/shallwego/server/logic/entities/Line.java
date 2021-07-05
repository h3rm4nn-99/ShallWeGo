package com.shallwego.server.logic.entities;

import com.shallwego.server.logic.service.LineCompositeKey;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@IdClass(LineCompositeKey.class)
public class Line implements Serializable {

    public Line() {}

    @Id
    private String identifier;

    @ManyToOne
    @Id
    private Company company;

    private String firstTerminus;
    private String secondTerminus;

    @ManyToMany(mappedBy = "lines")
    private List<Stop> stops;

    @ManyToMany(mappedBy = "linesAffectedEvent")
    private List<TemporaryEventReport> eventReports;

    @OneToOne(mappedBy = "lineAffected")
    private LineReport lineReport;

    @OneToMany(mappedBy = "targetLine")
    private List<DateAndTimesOfRides> dateAndTimesOfRidesByLine;

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

    public String getFirstTerminus() {
        return firstTerminus;
    }

    public void setFirstTerminus(String firstTerminus) {
        this.firstTerminus = firstTerminus;
    }

    public String getSecondTerminus() {
        return secondTerminus;
    }

    public void setSecondTerminus(String secondTerminus) {
        this.secondTerminus = secondTerminus;
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
