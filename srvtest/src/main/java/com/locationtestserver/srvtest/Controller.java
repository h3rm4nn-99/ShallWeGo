package com.locationtestserver.srvtest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

@RestController
public class Controller {
    public static ArrayList<UserEntity> users;
    @PutMapping("/api/putLocation")
    public String printLocation(@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude) {
        System.out.println("Latitudine " + latitude + "\nLongitudine " + longitude);
        return "OKAY\nLatitudine " + latitude + "\nLongitudine " + longitude;
    }
    @GetMapping("/api/createPeople")
    public String createPeople() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Random r = new Random();
        JSONArray array = (JSONArray) parser.parse(new FileReader("comuni.json"));
        Iterator<JSONObject> iterator = array.iterator();
        JSONObject result = new JSONObject();
        users = new ArrayList<>();

        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();
            JSONObject provinciaCurrent = (JSONObject) obj.get("provincia");
            String provinciaString = (String) provinciaCurrent.get("nome");

            if (provinciaString.equalsIgnoreCase("Salerno")) {
                byte[] byteArray = new byte[11];
                new Random().nextBytes(byteArray);
                String username = new String(byteArray, StandardCharsets.UTF_8);
                double karma = r.nextDouble() + r.nextInt(55);
                int permanenza = r.nextInt(365);
                String comune = (String) obj.get("nome");
                users.add(new UserEntity(username, comune, karma, permanenza));
            }
        }

        JSONArray usersJson = new JSONArray();
        Population<Individual> population = new Population<>();
        for (int j = 0; j < 10; j++) {
            Individual individual = new Individual();
            for (int i = 0; i < 5; i++) {

                UserEntity user = users.get(r.nextInt(users.size() - 1));
                individual.addUser(user);
                /*JSONObject obj = new JSONObject();

                obj.put("usernameCandidato", user.getUserName());
                obj.put("karma", user.getKarma());
                obj.put("permanenza", user.getPermanenzaSullaPiattaforma());
                obj.put("comune", user.getComune());
                usersJson.add(obj);*/
            }
            population.addIndividual(individual);
        }
        Location location = new Location(40.7415603, 14.6715039);
        HashMap<Individual, Double> normalizedFitness = Utils.getNormalizedFitness(population, location);
        Population<Individual> startPopulation = population;
        Population<Individual> bestPopulation = population;
        int generationsWithoutImprovement = 0;
        int i;
        int probability = 70;
        for (i = 0; i < 25; i++) {

            Population<Individual> selectedPopulation = new RouletteWheel(startPopulation).run(location);
            if (selectedPopulation.isEmpty()) {
                return "popolazione vuota";
            }
            System.out.println("Selected population " + selectedPopulation.getAverageFitness(location) + " Size " + selectedPopulation.getPopulationSize());

            if (selectedPopulation.getPopulationSize() >= 5) {
                probability /= 2;
            } else {
                probability *= 2;
            }
            Population<Individual> crossoveredPopulation = SinglePointCrossover.execute(selectedPopulation, probability);
            if (crossoveredPopulation.isEmpty()) {
                return "popolazione vuota";
            }
            System.out.println("Crossovered population " + crossoveredPopulation.getAverageFitness(location) + " Size " + crossoveredPopulation.getPopulationSize());
            Population<Individual> mutatedPopulation = MutationSubstitution.mutate(crossoveredPopulation);

            if (mutatedPopulation.isEmpty()) {
                return "popolazione vuota";
            }
            System.out.println("Mutated population " + mutatedPopulation.getAverageFitness(location) + " Size " + mutatedPopulation.getPopulationSize());
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
        }
        System.out.println("Fine del processo. Fitness popolazione: " + bestPopulation.getAverageFitness(location));
        return "Iterazione " + i + " " + bestPopulation.toString();
    }
}