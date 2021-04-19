package com.locationtestserver.srvtest;

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
                UserEntity[] usersArray = Arrays.copyOf(usersTempArray, usersTempArray.length, UserEntity[].class);
                int randomIndex = r.nextInt(usersArray.length);
                int randomCandidateIndex = r.nextInt(Controller.users.size() - 1);
                UserEntity candidate = Controller.users.get(randomCandidateIndex);
                Set<UserEntity> sentinel = new HashSet<>(individual.getUsers());
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
