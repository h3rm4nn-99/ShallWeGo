package com.shallwego.server.ga;

import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.ga.entities.UserGA;
import com.shallwego.server.logic.entities.User;
import com.shallwego.server.controller.Controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MutationSubstitution {
    public static Population<Individual> mutate(AlgorithmRunner runner, Population<Individual> population) {
        Random r = new Random();
        for (Individual individual: population.getIndividuals()) {
            int number = r.nextInt(11);
            if (number < 10) {
                Object[] usersTempArray = individual.getUsers().toArray();
                UserGA[] usersArray = Arrays.copyOf(usersTempArray, usersTempArray.length, UserGA[].class);
                int randomIndex = r.nextInt(usersArray.length);
                int randomCandidateIndex = r.nextInt(Controller.users.size() - 1);
                UserGA candidate = runner.pool.get(randomCandidateIndex);
                Set<UserGA> sentinel = new HashSet<>(individual.getUsers());
                while (sentinel.contains(candidate)) {
                    candidate = runner.pool.get(r.nextInt(runner.pool.size() - 1));
                }
                usersArray[randomIndex] = candidate;
                individual.setUsers(new HashSet<>(Arrays.asList(usersArray)));
            }
        }
        return population;
    }
}
