package com.shallwego.server.service;

import com.shallwego.server.logic.entities.Line;
import com.shallwego.server.logic.entities.Report;
import com.shallwego.server.logic.entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PendingStopReport extends PendingReport {

    private HashMap<Line, List<String>> destinations;

    public PendingStopReport(int id,
                             Report report, Set<User> verifiers, HashMap<Line, List<String>> destinations) {
        super(id, report, verifiers);
        this.destinations = destinations;
    }

    public HashMap<Line, List<String>> getDestinations() {
        return destinations;
    }

    public void setDestinations(HashMap<Line, List<String>> destinations) {
        this.destinations = destinations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingStopReport)) return false;
        if (!super.equals(o)) return false;
        PendingStopReport that = (PendingStopReport) o;
        return Objects.equals(getDestinations(), that.getDestinations());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDestinations());
    }

    @Override
    public String toString() {
        return "PendingStopReport{" + super.toString() +
                "destinations=" + destinations +
                '}';
    }
}
