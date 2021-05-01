package com.locationtestserver.srvtest.service;

import com.locationtestserver.srvtest.controller.Controller;
import com.locationtestserver.srvtest.ga.entities.Individual;
import com.locationtestserver.srvtest.ga.entities.Population;
import com.locationtestserver.srvtest.logic.entities.User;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Utils {

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

    public static Individual createRandomIndividual() {
        ArrayList<User> users = Controller.users;
        Individual individual = new Individual();
        for (int i = 0; i < 5; i++) {
            individual.addUser(users.get(new Random().nextInt(users.size())));
        }
        return individual;
    }


}
