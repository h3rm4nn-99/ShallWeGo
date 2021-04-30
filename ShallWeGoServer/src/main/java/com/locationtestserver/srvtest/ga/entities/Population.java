package com.locationtestserver.srvtest.ga.entities;

import com.locationtestserver.srvtest.service.Location;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;

public class Population<T extends Individual> {
    private List<Individual> individualList;

    public Population() {
        this.individualList = new ArrayList<>();
    }

    public List<Individual> getIndividuals() {
        return individualList;
    }

    public Integer getPopulationSize() {
        return individualList.size();
    }

    public boolean isEmpty() {
        return individualList.isEmpty();
    }

    public boolean addIndividual(Individual individual) {
        return individualList.add(individual);
    }

    public HashMap<Individual, Double> getPopulationFitness(Location location) throws IOException, ParseException {
        HashMap<Individual, Double> fitness = new HashMap<>();
        if (individualList.isEmpty()) {
            throw new IllegalStateException("Popolazione vuota!");
        }

        for (Individual i: individualList) {
            fitness.put(i, i.getFitness(location));
        }

        return fitness;
    }

    public double getAverageFitness(Location location) throws IOException, ParseException {
        if (individualList.isEmpty()) {
            throw new IllegalStateException("Popolazione vuota!");
        }

        double sum = 0.0;
        for (Individual i: individualList) {
            sum += i.getFitness(location);
        }

        return sum / individualList.size();
    }

    public Individual getBestIndividual(Location location) throws IOException, ParseException {
        Individual bestIndividual = null;
        double bestIndividualFitness = 0.0;

        for (Individual individual: individualList) {
            double currentIndividualFitness = individual.getFitness(location);
            if (bestIndividual == null || currentIndividualFitness > bestIndividualFitness) {
                bestIndividual = individual;
                bestIndividualFitness = currentIndividualFitness;
            }
        }

        if (bestIndividual == null) {
            throw new IllegalStateException("Popolazione vuota");
        }
        return bestIndividual;
    }

    public void removeIndividualAt(int index) {
        if (index < 0 || index > individualList.size() - 1) {
            throw new IllegalArgumentException();
        }

        individualList.remove(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Population)) return false;
        Population<?> that = (Population<?>) o;
        return individualList.equals(that.individualList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(individualList);
    }
}
