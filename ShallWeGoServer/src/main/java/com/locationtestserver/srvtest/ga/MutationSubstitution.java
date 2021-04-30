package com.locationtestserver.srvtest.ga;

import com.locationtestserver.srvtest.ga.entities.Individual;
import com.locationtestserver.srvtest.ga.entities.Population;
import com.locationtestserver.srvtest.logic.entities.User;
import com.locationtestserver.srvtest.controller.Controller;
import com.locationtestserver.srvtest.service.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MutationSubstitution {
    public static Population<Individual> mutate(Population<Individual> population) {
        Random r = new Random();
        Population newPopulation = new Population();
        for (Individual individual: population.getIndividuals()) {
            if (r.nextInt(10) < 7) {
                individual = Utils.createRandomIndividual();
            }
            newPopulation.addIndividual(individual);
        }
        return newPopulation;
    }
}
