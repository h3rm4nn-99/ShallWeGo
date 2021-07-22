package com.shallwego.server.ga.entities;

import com.shallwego.server.logic.entities.User;
import com.shallwego.server.service.Location;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class UserGA {
    private User target;
    private double fitness;

    public UserGA(User target) {
        this.target = target;
        this.fitness = -1d;
    }

    public UserGA() {}

    public User getTarget() {
        return target;
    }

    public void setTarget(User target) {
        this.target = target;
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
        if (distance < 3) {
            distancePartialFitness = 100 / (distance + 0.3); //avoid division by 0
        } else {
            distancePartialFitness = Math.pow(30 / distance, 2);
        }
        double karmaPartialFitness;

        if (target.getKarma() < 42) {
            karmaPartialFitness = target.getKarma();
        } else {
            karmaPartialFitness = target.getKarma() * 2;
        }

        this.fitness = (20 * distancePartialFitness) + (2 * karmaPartialFitness) / 2;

        return this.fitness;
    }
}
