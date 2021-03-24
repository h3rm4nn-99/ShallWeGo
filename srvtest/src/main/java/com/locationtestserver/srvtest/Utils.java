package com.locationtestserver.srvtest;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Utils {

    public static HashMap<Individual<UserEntity>, Double> getNormalizedFitness(Population<Individual<UserEntity>> population, Location location) throws IOException, ParseException {
        HashMap<Individual<UserEntity>, Double> normalizedFitness = new HashMap<>();
        HashMap<Individual<UserEntity>, Double> usersWithFitness = population.getPopulationFitness(location);
        ArrayList<Double> fitnessValues = new ArrayList<>();

        for (Individual<UserEntity> individual: usersWithFitness.keySet()) {
            fitnessValues.add(usersWithFitness.get(individual));
        }

        double sumOfFitnessValues = fitnessValues.stream().mapToDouble(value -> value).sum(); //Please forgive me

        for (Individual<UserEntity> user: usersWithFitness.keySet()) {
            normalizedFitness.put(user, (usersWithFitness.get(user) / sumOfFitnessValues));
        }
        return normalizedFitness;
    }
}
