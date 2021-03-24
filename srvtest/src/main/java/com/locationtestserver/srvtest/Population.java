package com.locationtestserver.srvtest;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Population<T extends Individual> {
    private Set<Individual<UserEntity>> individualSet;

    public Population() {
        this.individualSet = new HashSet<>();
    }

    public Set<Individual<UserEntity>> getIndividuals() {
        return individualSet;
    }

    public Integer getPopulationSize() {
        return individualSet.size();
    }

    public boolean addIndividual(Individual<UserEntity> individual) {
        return individualSet.add(individual);
    }

    public HashMap<Individual<UserEntity>, Double> getPopulationFitness(Location location) throws IOException, ParseException {
        HashMap<Individual<UserEntity>, Double> fitness = new HashMap<>();
        if (individualSet.isEmpty()) {
            throw new IllegalStateException("Popolazione vuota!");
        }

        for (Individual<UserEntity> i: individualSet) {
            fitness.put(i, i.getFitness(location));
        }

        return fitness;
    }
}
