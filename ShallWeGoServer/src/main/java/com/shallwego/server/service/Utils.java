package com.shallwego.server.service;

import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Utils {

    public static HashMap<String, Location> alreadyKnownLocations = new HashMap<>();

    public static HashMap<Individual, Double> getNormalizedFitness(Population population, Location location) throws IOException, ParseException {
        HashMap<Individual, Double> normalizedFitness = new HashMap<>();
        HashMap<Individual, Double> usersWithFitness = population.getPopulationFitness(location);
        ArrayList<Double> fitnessValues = new ArrayList<>();

        for (Individual individual: usersWithFitness.keySet()) {
            fitnessValues.add(usersWithFitness.get(individual));
        }

        double sumOfFitnessValues = fitnessValues.stream().mapToDouble(value -> value).sum(); //Please forgive me

        for (Individual user: usersWithFitness.keySet()) {
            normalizedFitness.put(user, (usersWithFitness.get(user) / sumOfFitnessValues));
        }
        return normalizedFitness;
    }
}
