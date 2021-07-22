package com.shallwego.server.ga;

import com.shallwego.server.controller.Controller;
import com.shallwego.server.ga.entities.Couple;
import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.ga.entities.UserGA;
import com.shallwego.server.logic.entities.User;

import java.util.*;

public class SinglePointCrossover {
    public static Population<Individual> execute(AlgorithmRunner runner, Population<Individual> parents, int probability) {
        Population<Individual> newPopulation = new Population<>();
        Set<Couple> couples = new HashSet<>();
        int size = parents.getPopulationSize();
        if (size == 1) {
            return parents;
        }

        if (size == 2) {
            Individual parent1 = (Individual) parents.getIndividuals().toArray()[0];
            Individual parent2 = (Individual) parents.getIndividuals().toArray()[1];

            Couple children = cross(parent1, parent2);

            newPopulation.addIndividual(children.getindividual1());
            newPopulation.addIndividual(children.getindividual2());
            return newPopulation;
        }

        Random r = new Random();

        for (int i = 0; i < size; i++) {

            int killer = r.nextInt(100);

            if (killer > probability) {
                continue;
            }

            Individual parent1 = null;
            Individual parent2 = null;

            int stop = 0;
            int sentinel = 0;
            int random1 = r.nextInt(size);
            parent1 = (Individual) parents.getIndividuals().toArray()[random1];
            Set<Individual> alreadyCoupled = new HashSet<>();
            do {
                if (alreadyCoupled.size() == size - 1) {
                    sentinel = 1;
                    break;
                }
                int random2 = r.nextInt(size);
                parent2 = (Individual) parents.getIndividuals().toArray()[random2];
                Couple family = new Couple(parent1, parent2);
                if (!parent1.equals(parent2) && !couples.contains(family)) {
                    couples.add(family);
                    stop = 1;
                } else {
                    if (!parent1.equals(parent2)) {
                        alreadyCoupled.add(parent2);
                    }
                }
            } while (stop == 0);

            if (sentinel == 1) {
                continue;
            }

            Couple tempChildren = cross(parent1, parent2);
            Couple children = fixChildrenSize(runner, tempChildren);
            newPopulation.addIndividual(children.getindividual1());
            newPopulation.addIndividual(children.getindividual2());
        }

        return newPopulation;
    }

    private static Couple cross(Individual parent1, Individual parent2) {
        Object[] tempParent1Array = parent1.getUsers().toArray();
        Object[] tempParent2Array = parent2.getUsers().toArray();
        UserGA[] parent1Array = Arrays.copyOf(tempParent1Array, tempParent1Array.length, UserGA[].class);
        UserGA[] parent2Array = Arrays.copyOf(tempParent2Array, tempParent2Array.length, UserGA[].class);
        int parent1Size = parent1Array.length;
        int parent2Size = parent2Array.length;

        int j, k;

        List<UserGA> child1 = new ArrayList<>();

        for (j = 0; j < parent1Size / 2; j++) {
            child1.add(parent1Array[j]);
        }

        for (k = parent2Size / 2; k < parent2Size; k++) {
            child1.add(parent2Array[k]);
        }

        List<UserGA> child2 = new ArrayList<>();

        for (j = 0; j < parent2Size / 2; j++) {
            child2.add(parent2Array[j]);
        }

        for (k = parent1Size / 2; k < parent1Size; k++) {
            child2.add(parent1Array[k]);
        }

        Individual child1Individual = new Individual();

        for (UserGA user: child1) {
            child1Individual.addUser(user);
        }

        Individual child2Individual = new Individual();

        for (UserGA user: child2) {
            child2Individual.addUser(user);
        }

        return new Couple(child1Individual, child2Individual);
    }

    private static Couple fixChildrenSize(AlgorithmRunner runner, Couple children) {
        Couple fixedChildren = new Couple(null, null);
        Individual child1 = children.getindividual1();
        Individual child2 = children.getindividual2();

        if (child1.getSize() < AlgorithmRunner.INDIVIDUAL_SIZE) {
            addRandomUsers(runner, child1, AlgorithmRunner.INDIVIDUAL_SIZE - child1.getSize());
        }

        if (child2.getSize() < AlgorithmRunner.INDIVIDUAL_SIZE) {
            addRandomUsers(runner, child2, AlgorithmRunner.INDIVIDUAL_SIZE - child2.getSize());
        }

        fixedChildren.setindividual1(child1);
        fixedChildren.setindividual2(child2);

        return fixedChildren;
    }

    private static void addRandomUsers(AlgorithmRunner runner, Individual individual, int howMany) {
        Random r = new Random();
        for (int i = 0; i < howMany; i++) {
            int randomCandidateIndex = r.nextInt(runner.pool.size() - 1);
            UserGA candidate = runner.pool.get(randomCandidateIndex);
            while (individual.getUsers().contains(candidate)) {
                candidate = runner.pool.get(r.nextInt(runner.pool.size() - 1));
            }
            individual.addUser(candidate);
        }
    }
}