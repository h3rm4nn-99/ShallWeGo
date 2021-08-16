package com.shallwego.server.rides;

import com.shallwego.server.logic.entities.Line;
import com.shallwego.server.logic.entities.Stop;
import com.shallwego.server.logic.entities.User;
import com.shallwego.server.service.Location;

import java.util.List;
import java.util.Objects;

public class Ride {
    public static int LAST_ID = 0;
    private int id;
    private Line line;
    private String destination;
    private Location lastLocation;
    private User user;

    private List<String> notes;
    private int crowding;
    private boolean hasAirConditioning;

    public Ride(Line line, String destination, Location lastLocation) {
        synchronized(this) {
           this.id = LAST_ID++;
        }
        this.line = line;
        this.destination = destination;
        this.lastLocation = lastLocation;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public int getCrowding() {
        return crowding;
    }

    public void setCrowding(int crowding) {
        this.crowding = crowding;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isHasAirConditioning() {
        return hasAirConditioning;
    }

    public void setHasAirConditioning(boolean hasAirConditioning) {
        this.hasAirConditioning = hasAirConditioning;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ride)) return false;
        Ride ride = (Ride) o;
        return getId() == ride.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
