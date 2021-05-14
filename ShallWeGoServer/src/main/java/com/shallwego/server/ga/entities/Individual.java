package com.shallwego.server.ga.entities;

import com.shallwego.server.service.Location;
import com.shallwego.server.logic.entities.User;
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
            double distance = location.distance(entity.getComune());
            double distancePartialFitness;
            if (distance == 0) {
                distancePartialFitness = 100 / (distance + 0.3); //avoid division by 0
            } else {
                distancePartialFitness = Math.pow(30 / distance, 2);
            }

            double karmaPartialFitness;

            if (entity.getKarma() < 42) {
                karmaPartialFitness = entity.getKarma();
            } else {
                karmaPartialFitness = entity.getKarma() * 2;
            }
            fitness += (4 * distancePartialFitness) + (2 * karmaPartialFitness) / 2;
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
        return users.equals(that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }

}
