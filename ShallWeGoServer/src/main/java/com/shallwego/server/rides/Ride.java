package com.shallwego.server.rides;

import com.shallwego.server.logic.entities.Line;
import com.shallwego.server.logic.entities.Stop;
import com.shallwego.server.service.Location;

public class Ride {
    public static int LAST_ID = 0;
    private int id;
    private Line line;
    private String destination;
    private Location lastLocation;

    private String notes;
    private int crowding;

    public Ride(Line line, String destination, Stop firstStop, Location lastLocation) {
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
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
}
