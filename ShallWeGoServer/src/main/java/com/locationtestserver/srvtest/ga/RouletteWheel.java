package com.locationtestserver.srvtest.ga;

import com.locationtestserver.srvtest.ga.entities.Individual;
import com.locationtestserver.srvtest.service.Location;
import com.locationtestserver.srvtest.ga.entities.Population;
import com.locationtestserver.srvtest.service.Utils;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RouletteWheel {
   private Population<Individual> population;

   public RouletteWheel(Population<Individual> population) {
       this.population = population;
   }

   public Population<Individual> run(Location location) throws IOException, ParseException {
       Population<Individual> target = new Population<>();

       HashMap<Individual, Double> normalizedFitness = Utils.getNormalizedFitness(this.population, location);
       List<WheelElement> wheel = new ArrayList<>();
       double tracker = 0D;
       int j = 0;
       for (Individual individual: normalizedFitness.keySet()) {
            System.out.println("Individual " + j++ + " " + individual.getFitness(location));
            wheel.add(new WheelElement(individual, tracker, normalizedFitness.get(individual)));
            tracker += normalizedFitness.get(individual);
       }

       for (int i = 0; i < wheel.size(); i++) {
           double chooser = Math.random();
           System.out.println("Chooser: " + chooser);
           for (WheelElement element: wheel) {
               if (element.startPosition <= chooser && chooser <= element.startPosition + element.length) {
                   target.addIndividual(element.individual);
                   break;
               }
           }
       }
       return target;
   }

   private class WheelElement {
       private Individual individual;
       private double startPosition;
       private double length;

       private WheelElement(Individual individual, double startPosition, double length) {
           this.individual = individual;
           this.startPosition = startPosition;
           this.length = length;
       }
   }
}
