package com.locationtestserver.srvtest;

import org.apache.catalina.User;
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
       for (Individual individual: normalizedFitness.keySet()) {
            wheel.add(new WheelElement(individual, tracker, normalizedFitness.get(individual)));
            tracker += normalizedFitness.get(individual);
       }

       Random random = new Random();
       for (int i = 0; i < wheel.size(); i++) {
           double chooser = random.nextDouble();
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
