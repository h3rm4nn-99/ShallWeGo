package com.shallwego.server.ga.entities;

import com.shallwego.server.logic.entities.User;
import com.shallwego.server.service.Location;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Objects;

public class UserGA {
    private User target;
    private double fitness;


    public UserGA(User target) {
        this.target = target;
        this.fitness = -1d;
    }

    public User getTarget() {
        return target;
    }


    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getFitness(Location location) throws IOException, ParseException {
        if (this.fitness != -1d) {
            return this.fitness;
        }
        double distance = location.distance(target.getComune());
        double distancePartialFitness;
        if (distance < 15) {
            distancePartialFitness = 250 + (100 / (distance + 0.3)); //avoid division by 0
        } else {
            distancePartialFitness = Math.pow(30 / distance, 2);
        }

        this.fitness = (4 * distancePartialFitness) + (2 * this.getTarget().getKarma()) / 2;

        return this.fitness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGA)) return false;
        UserGA userGA = (UserGA) o;
        return Double.compare(userGA.fitness, fitness) == 0 && Objects.equals(getTarget(), userGA.getTarget());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTarget(), fitness);
    }
}
