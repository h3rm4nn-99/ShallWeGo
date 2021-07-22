package com.shallwego.server.ga;

import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.ga.entities.UserGA;
import com.shallwego.server.logic.entities.User;
import com.shallwego.server.service.Location;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.*;

public class AlgorithmRunner {

    public static final int INDIVIDUAL_SIZE = 5;
    public static final int POPULATION_SIZE = 10;

    public List<UserGA> pool;
    private Location location;
    private Population<Individual> population;

    private AlgorithmRunner(Location location) {
        this.location = location;
        this.population = new Population<>();
        this.pool = new ArrayList<>();
    }

    public static AlgorithmRunner buildPopulation(List<User> users, Location location) {
        AlgorithmRunner instance = new AlgorithmRunner(location);

        for (User user : users) {
            instance.pool.add(new UserGA(user));
        }

        Random r = new Random();
        for (int j = 0; j < AlgorithmRunner.POPULATION_SIZE; j++) {
            Individual individual = new Individual();
            for (int i = 0; i < AlgorithmRunner.INDIVIDUAL_SIZE; i++) {
                UserGA user = instance.pool.get(r.nextInt(instance.pool.size() - 1));
                if (individual.getUsers().contains(user)) {
                    i--;
                    continue;
                }
                individual.addUser(user);
            }
            instance.population.addIndividual(individual);
        }

        return instance;
    }

    public Set<User> run() throws IOException, ParseException {
        Population<Individual> bestPopulation = this.population;
        Population<Individual> archive = new Population<>();
        int generationsWithoutImprovement = 0;
        int i;
        int probability = 70;
        long startTime = ZonedDateTime.now().toInstant().toEpochMilli();
        for (i = 0; i < 25; i++) {

            Population<Individual> selectedPopulation = new RouletteWheel(population).run(location);
            if (selectedPopulation.isEmpty()) {
                return null;
            }

            Individual candidate = selectedPopulation.getBestIndividual(location);
            double candidateFitness = candidate.getFitness(location);
            System.out.println("Selected population " + selectedPopulation.getAverageFitness(location) + " Size " + selectedPopulation.getPopulationSize());

            if (selectedPopulation.getPopulationSize() >= 15) {
                probability /= 2;
            } else {
                probability *= 2;
                if (probability >= 100) {
                    probability = 100;
                }
            }
            Population<Individual> crossoveredPopulation = SinglePointCrossover.execute(this, selectedPopulation, probability);
            if (crossoveredPopulation.isEmpty()) {
                return null;
            }

            Individual bestIndividualAfterCrossover = crossoveredPopulation.getBestIndividual(location);
            double bestIndividualAfterCrossoverFitness = bestIndividualAfterCrossover.getFitness(location);

            if (bestIndividualAfterCrossoverFitness > candidateFitness) {
                candidate = bestIndividualAfterCrossover;
                candidateFitness = bestIndividualAfterCrossoverFitness;
            }

            System.out.println("Crossovered population " + crossoveredPopulation.getAverageFitness(location) + " Size " + crossoveredPopulation.getPopulationSize());

            Population<Individual> mutatedPopulation = MutationSubstitution.mutate(this, crossoveredPopulation);

            if (mutatedPopulation.isEmpty()) {
                return null;
            }
            Individual bestIndividualAfterMutation = mutatedPopulation.getBestIndividual(location);
            double bestIndividualAfterMutationFitness = bestIndividualAfterMutation.getFitness(location);

            if (bestIndividualAfterMutationFitness > candidateFitness) {
                candidate = bestIndividualAfterMutation;
            }
            System.out.println("Mutated population " + mutatedPopulation.getAverageFitness(location) + " Size " + mutatedPopulation.getPopulationSize());
            archive.addIndividual(candidate);

            if (mutatedPopulation.getAverageFitness(location) > bestPopulation.getAverageFitness(location)) {
                generationsWithoutImprovement = 0;
                bestPopulation = mutatedPopulation;
            } else {
                generationsWithoutImprovement++;
                if (generationsWithoutImprovement == 3) {
                    break;
                }
            }

            long currentTime = ZonedDateTime.now().toInstant().toEpochMilli();
            if (currentTime - startTime >= 150000) {
                break;
            }
            this.population = bestPopulation;
        }

        if (bestPopulation.getAverageFitness(location) <= archive.getAverageFitness(location)) {
            bestPopulation = archive;
        }


        System.out.println("Fine del processo. Fitness popolazione: " + this.population.getAverageFitness(location));

        int counter = 0;
        for (Individual individual: this.population.getIndividuals()) {
            System.out.println("Individuo " + counter);
            for (UserGA user: individual.getUsers()) {
                System.out.print(user.getTarget().getComune() + " ");
            }
            System.out.println();
            counter++;
        }
        Set<UserGA> output = new HashSet<>();

        for (Individual individual: bestPopulation.getIndividuals()) {
            List<UserGA> userList = new ArrayList<>(individual.getUsers());
            userList.sort((user1, user2) -> {
                double user1fitness = 0d;
                double user2fitness = 0d;
                try {
                    user1fitness = user1.getFitness(location);
                    user2fitness = user2.getFitness(location);

                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
                if (user1fitness < user2fitness) {
                    return -1;
                } else if (user1fitness == user2fitness) {
                    return 0;
                } else {
                    return 1;
                }
            });

            for (int k = userList.size() - 1; k >= 0; k--) {
                if (!output.contains(userList.get(k))) {
                    output.add(userList.get(k));
                    break;
                }
            }
        }

        if (output.size() >= 5) {
            List<UserGA> cutList = new ArrayList<>(output);
            cutList.sort((user1, user2) -> {
                double user1fitness = 0d;
                double user2fitness = 0d;
                try {
                    user1fitness = user1.getFitness(location);
                    user2fitness = user2.getFitness(location);

                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
                if (user1fitness < user2fitness) {
                    return -1;
                } else if (user1fitness == user2fitness) {
                    return 0;
                } else {
                    return 1;
                }
            });

            Collections.reverse(cutList);

            output = new HashSet<>(cutList.subList(0, 5));
        }
        Set<User> bestUsers = new HashSet<>();
        for (UserGA user: output) {
            bestUsers.add(user.getTarget());
        }

        return bestUsers;
    }
}
