package com.shallwego.server.ga;

import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.service.Location;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.time.ZonedDateTime;

public class AlgorithmRunner {

    public static final int INDIVIDUAL_SIZE = 5;
    public static final int POPULATION_SIZE = 10;

    public static Population<Individual> run(Population<Individual> startPopulation, Location location) throws IOException, ParseException {
        Population<Individual> bestPopulation = startPopulation;
        Population<Individual> archive = new Population<>();
        int generationsWithoutImprovement = 0;
        int i;
        int probability = 70;
        long startTime = ZonedDateTime.now().toInstant().toEpochMilli();
        for (i = 0; i < 25; i++) {

            Population<Individual> selectedPopulation = new RouletteWheel(startPopulation).run(location);
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
            Population<Individual> crossoveredPopulation = SinglePointCrossover.execute(selectedPopulation, probability);
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

            Population<Individual> mutatedPopulation = MutationSubstitution.mutate(crossoveredPopulation);

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
            startPopulation = mutatedPopulation;

            long currentTime = ZonedDateTime.now().toInstant().toEpochMilli();
            if (currentTime - startTime >= 150000) {
                break;
            }
        }

        if (bestPopulation.getAverageFitness(location) <= archive.getAverageFitness(location)) {
            bestPopulation = archive;
        }
        System.out.println("Fine del processo. Fitness popolazione: " + bestPopulation.getAverageFitness(location));
        return bestPopulation;
    }
}
