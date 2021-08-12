package com.shallwego.server.logic.entities;

import com.shallwego.server.logic.service.DestinationsByStopAndLineRepository;
import com.shallwego.server.logic.service.LineCompositeKey;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity

@IdClass(LineCompositeKey.class)
public class Line implements Serializable {


    public Line() {}

    @Id
    @Column(length = 100)
    private String identifier;

    @Id
    @JoinColumn(name = "name", referencedColumnName = "name")
    @ManyToOne
    private Company company;

    @ElementCollection
    private List<String> destinations;

    @ManyToMany(mappedBy = "linesAffectedEvent")
    private List<TemporaryEventReport> eventReports;

    @ManyToMany(mappedBy = "lines")
    private List<Stop> stops;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "lineAffected")
    private LineReport lineReport;

    @OneToMany(mappedBy = "targetLine")
    private List<DateAndTimesOfRides> dateAndTimesOfRidesByLine = new ArrayList<>();

    @OneToMany(mappedBy = "targetLine", fetch = FetchType.EAGER)
    private List<DestinationsByStopAndLine> destinationsByLine = new ArrayList<>(); //needed for the many-to-many relation.

    @OneToMany(mappedBy = "targetLine")
    private List<DestinationsByReportAndLine> destinationsByLineReport = new ArrayList<>();

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

    public List<DestinationsByReportAndLine> getDestinationsByLineReport() {
        return destinationsByLineReport;
    }

    public void setDestinationsByLineReport(List<DestinationsByReportAndLine> destinationsByLineReport) {
        this.destinationsByLineReport = destinationsByLineReport;
    }

    public List<DateAndTimesOfRides> getDateAndTimesOfRidesByLine() {
        return dateAndTimesOfRidesByLine;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<String> destinations) {
        this.destinations = destinations;
    }

    public void setDateAndTimesOfRidesByLine(List<DateAndTimesOfRides> dateAndTimesOfRidesByLine) {
        this.dateAndTimesOfRidesByLine = dateAndTimesOfRidesByLine;
    }

    public List<DestinationsByStopAndLine> getDestinationsByLine() {
        return destinationsByLine;
    }

    public void setDestinationsByLine(List<DestinationsByStopAndLine> destinationsByLine) {
        this.destinationsByLine = destinationsByLine;
    }

    public void addDestinationByLine(DestinationsByStopAndLine destinationsByStopAndLine) {
        this.destinationsByLine.add(destinationsByStopAndLine);
    }

    public List<TemporaryEventReport> getEventReports() {
        return eventReports;
    }

    public void setEventReports(List<TemporaryEventReport> eventReports) {
        this.eventReports = eventReports;
    }

    public void addTemporaryEvent(TemporaryEventReport report) {
        this.eventReports.add(report);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Line)) return false;
        Line line = (Line) o;
        return Objects.equals(getIdentifier(), line.getIdentifier()) && Objects.equals(getCompany(), line.getCompany());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentifier(), getCompany());
    }
}
