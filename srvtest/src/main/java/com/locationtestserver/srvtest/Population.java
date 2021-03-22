package com.locationtestserver.srvtest;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Population<T extends Individual> {
    private Set<Individual> individualSet;

    private Population() {}

    public static Population<Individual> createPopulation() {
        Population<Individual> population = new Population<>();
        population.individualSet = new HashSet<>();
        return population;
    }

    public Set<Individual> getIndividuals() {
        return individualSet;
    }

    public Integer getPopulationSize() {
        return individualSet.size();
    }

    public boolean addIndividual(Individual individual) {
        return individualSet.add(individual);
    }

    public HashMap<Individual, Double> getPopulationFitness(Location location) throws IOException, ParseException {
        HashMap<Individual, Double> fitness = new HashMap<>();
        if (individualSet.isEmpty()) {
            throw new IllegalStateException("Popolazione vuota!");
        }

        for (Individual i: individualSet) {
            fitness.put(i, Individual.getFitness(i, location));
        }

        return fitness;
    }
}
