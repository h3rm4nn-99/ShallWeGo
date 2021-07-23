package com.shallwego.server.ga.entities;

import com.shallwego.server.service.Location;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Individual {
    private HashSet<UserGA> users;

    public Individual() {
        users = new HashSet<>();
    }

    public Individual(HashSet<UserGA> users) {
        this.users = new HashSet<>(users);
    }

    public HashSet<UserGA> getUsers() {
        return users;
    }

    public boolean addUser(UserGA e) {
        return users.add(e);
    }

    public void setUsers(HashSet<UserGA> users) {
        this.users = users;
    }

    public Integer getSize() {
        return users.size();
    }

    public double getFitness(Location location) throws IOException, ParseException {
        double fitness = 0.0;
        for (UserGA entity: this.getUsers()) {
            fitness += entity.getFitness(location);
        }
        return fitness / this.getSize();
    }

    @Override
    public String toString() {
        return "Individual{" +
                "users=" + users +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Individual)) return false;

        Individual that = (Individual) o;

        return getUsers().equals(that.getUsers());
    }

    @Override
    public int hashCode() {
        return getUsers().hashCode();
    }
}
