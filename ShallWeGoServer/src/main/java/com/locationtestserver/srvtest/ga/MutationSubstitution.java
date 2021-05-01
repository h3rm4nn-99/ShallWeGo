package com.locationtestserver.srvtest.ga;

import com.locationtestserver.srvtest.ga.entities.Individual;
import com.locationtestserver.srvtest.ga.entities.Population;
import com.locationtestserver.srvtest.logic.entities.User;
import com.locationtestserver.srvtest.controller.Controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MutationSubstitution {
    public static Population<Individual> mutate(Population<Individual> population) {
        Random r = new Random();
        for (Individual individual: population.getIndividuals()) {
            int number = r.nextInt(11);
            if (number < 10) {
                Object[] usersTempArray = individual.getUsers().toArray();
                User[] usersArray = Arrays.copyOf(usersTempArray, usersTempArray.length, User[].class);
                int randomIndex = r.nextInt(usersArray.length);
                int randomCandidateIndex = r.nextInt(Controller.users.size() - 1);
                User candidate = Controller.users.get(randomCandidateIndex);
                Set<User> sentinel = new HashSet<>(individual.getUsers());
                while (sentinel.contains(candidate)) {
                    candidate = Controller.users.get(r.nextInt(Controller.users.size() - 1));
                }
                usersArray[randomIndex] = candidate;
                individual.setUsers(new HashSet<>(Arrays.asList(usersArray)));
            }
        }
        return population;
    }
}
