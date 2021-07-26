package com.shallwego.server.logic.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Route implements Serializable {
    @Id @GeneratedValue
    private Integer id;

    @ManyToOne
    private Line line;

    private String pathname;

    @ElementCollection
    private List<Integer> stopIds = new ArrayList<>();

    public Route() {}

    public Route(String pathname) {
        this.pathname = pathname;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public List<Integer> getStopIds() {
        return stopIds;
    }

    public void setStopIds(List<Integer> stopIds) {
        this.stopIds = stopIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route route = (Route) o;
        return getLine().equals(route.getLine()) && getStopIds().equals(route.getStopIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLine(), getStopIds());
    }

    @Override
    public String toString() {
        return "Route{" +
                "line=" + line +
                ", stopIds=" + stopIds +
                '}';
    }
}
