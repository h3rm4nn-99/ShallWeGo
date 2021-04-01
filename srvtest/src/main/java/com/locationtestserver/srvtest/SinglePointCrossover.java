package com.locationtestserver.srvtest;

import org.apache.catalina.User;

import java.util.*;

public class SinglePointCrossover {
    public static Population<Individual> execute(Population<Individual> parents) {
        Population<Individual> newPopulation = new Population<>();
        Set<Couple> couples = new HashSet<>();
        int size = parents.getPopulationSize();
        if (size == 1) {
            return parents;
        }

        if (size == 2) {
            Individual parent1 = (Individual) parents.getIndividuals().toArray()[0];
            Individual parent2 = (Individual) parents.getIndividuals().toArray()[1];

            Object[] tempParent1Array = parent1.getUsers().toArray();
            Object[] tempParent2Array = parent2.getUsers().toArray();
            UserEntity[] parent1Array = Arrays.copyOf(tempParent1Array, tempParent1Array.length, UserEntity[].class);
            UserEntity[] parent2Array = Arrays.copyOf(tempParent2Array, tempParent2Array.length, UserEntity[].class);
            int parent1Size = parent1Array.length;
            int parent2Size = parent2Array.length;

            int j, k;

            List<UserEntity> child1 = new ArrayList<>();

            for (j = 0; j < parent1Size / 2; j++) {
                child1.add(parent1Array[j]);
            }

            for (k = (parent2Size / 2) + (parent2Size % 2 == 0 ? 0 : 1); k < parent2Size; k++) {
                child1.add(parent2Array[k]);
            }

            List<UserEntity> child2 = new ArrayList<>();

            for (j = 0; j < parent2Size / 2; j++) {
                child2.add(parent2Array[j]);
            }

            for (k = (parent1Size / 2) + (parent1Size % 2 == 0 ? 0 : 1); k < parent1Size; k++) {
                child2.add(parent1Array[k]);
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
            return newPopulation;
        }

        Random r = new Random();

        for (int i = 0; i < size; i++) {

            int killer = r.nextInt(42);

            if (killer < 15) {
                continue;
            }

            Individual parent1 = null;
            Individual parent2 = null;

            int stop = 0;
            int random1 = r.nextInt(size);
            parent1 = (Individual) parents.getIndividuals().toArray()[random1];

            do {
                int random2 = r.nextInt(size);
                parent2 = (Individual) parents.getIndividuals().toArray()[random2];
                Couple family = new Couple(parent1, parent2);
                if (!parent1.equals(parent2) && !couples.contains(family)) {
                    couples.add(family);
                    stop = 1;
                }
            } while (stop == 0);
            Object[] tempParent1Array = parent1.getUsers().toArray();
            Object[] tempParent2Array = parent2.getUsers().toArray();
            UserEntity[] parent1Array = Arrays.copyOf(tempParent1Array, tempParent1Array.length, UserEntity[].class);
            UserEntity[] parent2Array = Arrays.copyOf(tempParent2Array, tempParent2Array.length, UserEntity[].class);
            int parent1Size = parent1Array.length;
            int parent2Size = parent2Array.length;

            int j, k;

            List<UserEntity> child1 = new ArrayList<>();

            for (j = 0; j < parent1Size / 2; j++) {
                child1.add(parent1Array[j]);
            }

            for (k = (parent2Size / 2) + (parent2Size % 2 == 0 ? 0 : 1); k < parent2Size; k++) {
                child1.add(parent2Array[k]);
            }

            List<UserEntity> child2 = new ArrayList<>();

            for (j = 0; j < parent2Size / 2; j++) {
                child2.add(parent2Array[j]);
            }

            for (k = (parent1Size / 2) + (parent1Size % 2 == 0 ? 0 : 1); k < parent1Size; k++) {
                child2.add(parent1Array[k]);
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