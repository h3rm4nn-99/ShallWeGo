package com.locationtestserver.srvtest.ga.entities;

import com.locationtestserver.srvtest.service.Location;
import com.locationtestserver.srvtest.logic.entities.User;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Individual {
    private Set<User> users;

    public Individual() {
        users = new HashSet<>();
    }

    public Set<User> getUsers() {
        return users;
    }

    public boolean addUser(User e) {
        return users.add(e);
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Integer getSize() {
        return users.size();
    }

    public double getFitness(Location location) throws IOException, ParseException {
        double fitness = 0.0;
        for (User entity: this.getUsers()) {

            fitness += entity.getFitness(location);
        }
        return fitness / this.getSize();
    }

    public User getBestUser(Location location) throws IOException, ParseException {
        double bestFitness = 0.0;
        User best = null;
        for (User user: this.getUsers()) {
            double currentFitness = user.getFitness(location);
            if (currentFitness > bestFitness) {
                best = user;
                bestFitness = currentFitness;
            }
        }
        return best;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Individual)) return false;
        Individual that = (Individual) o;
        return users.equals(that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }

    @Override
    public String toString() {
        return "Individual{" +
                "users=" + users.toString() +
                '}';
    }
}
