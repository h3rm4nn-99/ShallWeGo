package com.locationtestserver.srvtest;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Individual {
    private final Set<UserEntity> users;

    public Individual() {
        users = new HashSet<>();
    }

    public Set<UserEntity> getUsers() {
        return users;
    }

    public boolean addUser(UserEntity e) {
        return users.add(e);
    }

    public Integer getSize() {
        return users.size();
    }

    public double getFitness(Location location) throws IOException, ParseException {
        double fitness = 0.0;
        for (UserEntity entity: this.getUsers()) {
            double distance = location.distance(entity.getComune());
            double distancePartialFitness = 15 / distance;
            double karmaPartialFitness;

            if (entity.getKarma() < 42) {
                karmaPartialFitness = Math.pow(entity.getKarma(), 0.6);
            } else {
                karmaPartialFitness = Math.pow(1.1, entity.getKarma());
            }
            double daysInPlatformFitness = entity.getPermanenzaSullaPiattaforma() / 2.0;
            fitness += ((3 * distancePartialFitness) + (2 * karmaPartialFitness) + daysInPlatformFitness) / 3;
        }
        return fitness / this.getSize();
    }

}
