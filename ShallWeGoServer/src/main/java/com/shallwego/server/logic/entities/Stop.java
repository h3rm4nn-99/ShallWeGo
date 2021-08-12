package com.shallwego.server.logic.entities;

import com.shallwego.server.service.Utils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Stop {

    @PostPersist
    public void putCrowding() {
        System.out.println("Nuova fermata aggiunta. Inizializzazione del valore di affollamento");
        Utils.stopCrowding.put(this.getId(), 0);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @ManyToMany
    private List<Line> lines = new ArrayList<>();

    @OneToOne(mappedBy = "stopReported", cascade = CascadeType.ALL)
    private StopReport stopReport;

    @OneToMany(mappedBy = "targetStop")
    private List<DateAndTimesOfRides> dateAndTimesOfRidesByStop = new ArrayList<>();

    @OneToMany(mappedBy = "targetStop")
    private List<DestinationsByStopAndLine> destinationsByStop = new ArrayList<>();

    @ManyToMany(mappedBy = "preferredStops")
    private List<User> userFavorites;

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

    public List<DateAndTimesOfRides> getDateAndTimesOfRidesByStop() {
        return dateAndTimesOfRidesByStop;
    }

    public void setDateAndTimesOfRidesByStop(List<DateAndTimesOfRides> dateAndTimesOfRidesByStop) {
        this.dateAndTimesOfRidesByStop = dateAndTimesOfRidesByStop;
    }

    public List<DestinationsByStopAndLine> getDestinationsByStop() {
        return destinationsByStop;
    }

    public void setDestinationsByStop(List<DestinationsByStopAndLine> destinationsByStop) {
        this.destinationsByStop = destinationsByStop;
    }

    public void addDestinationsByStop(DestinationsByStopAndLine destinationsByStopAndLine) {
        this.destinationsByStop.add(destinationsByStopAndLine);
    }

    public List<User> getUserFavorites() {
        return userFavorites;
    }

    public void setUserFavorites(List<User> userFavorites) {
        this.userFavorites = userFavorites;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stop)) return false;
        Stop stop = (Stop) o;
        return Objects.equals(getId(), stop.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }


    @Override
    public String toString() {
        return "Stop{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hasShelter=" + hasShelter +
                ", hasTimeTables=" + hasTimeTables +
                ", hasStopSign=" + hasStopSign +
                '}';
    }
}
