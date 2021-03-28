package com.locationtestserver.srvtest;

import org.apache.catalina.User;

import java.util.*;

public class SinglePointCrossover {
    public static Population<Individual> execute(Population<Individual> parents) {
        Population<Individual> newPopulation = new Population<>();
        int size = parents.getPopulationSize();
        Set<Individual> alreadyConsidered = new HashSet<>();

        Random r = new Random();

        int stop = 0;

        for (int i = 0; i < size; i++) {

            Individual parent1 = null;
            Individual parent2 = null;

            do {
                parent1 = (Individual) parents.getIndividuals().toArray()[r.nextInt(size - 1)];
                if (alreadyConsidered.contains(parent1)) {
                    continue;
                } else {
                    alreadyConsidered.add(parent1);
                    stop = 1;
                }
            } while (stop == 0);

            stop = 0;

            do {
                parent2 = (Individual) parents.getIndividuals().toArray()[r.nextInt(size - 1)];
                if (alreadyConsidered.contains(parent1) || parent1.equals(parent2)) {
                    continue;
                } else {
                    alreadyConsidered.add(parent2);
                    stop = 1;
                }
            } while (stop == 0);

            UserEntity[] parent1Array = (UserEntity[]) parent1.getUsers().toArray();
            UserEntity[] parent2Array = (UserEntity[]) parent2.getUsers().toArray();
            int parent1Size = parent1Array.length;
            int parent2Size = parent2Array.length;

            int j, k;

            List<UserEntity> child1 = new ArrayList<>();

            for (j = 0; j < parent1Size / 2; j++) {
                child1.add(parent1Array[i]);
            }

            for (k = parent2Size / 2; k < parent2Size; k++) {
                child1.add(parent2Array[k]);
            }

            List<UserEntity> child2 = new ArrayList<>();

            for (j = 0; j < parent2Size / 2; j++) {
                child2.add(parent2Array[j]);
            }

            for (k = parent1Size / 2; k < parent1Size; k++) {
                child2.add(parent2Array[k]);
            }

            Individual child1Individual = new Individual();

            for (UserEntity user: child1) {
                child1Individual.addUser(user);
            }

            Individual child2Individual = new Individual();

            for (UserEntity user: child2) {
                child2Individual.addUser(user);
            }

            newPopulation.addIndividual(child1Individual);
            newPopulation.addIndividual(child2Individual);
        }

        return newPopulation;
    }
}