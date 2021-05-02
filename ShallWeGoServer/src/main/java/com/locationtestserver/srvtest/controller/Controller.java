package com.locationtestserver.srvtest.controller;

import com.locationtestserver.srvtest.ga.MutationSubstitution;
import com.locationtestserver.srvtest.ga.RouletteWheel;
import com.locationtestserver.srvtest.ga.SinglePointCrossover;
import com.locationtestserver.srvtest.ga.entities.Individual;
import com.locationtestserver.srvtest.ga.entities.Population;
import com.locationtestserver.srvtest.logic.entities.User;
import com.locationtestserver.srvtest.service.Location;
import com.locationtestserver.srvtest.service.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
public class Controller {
    public static ArrayList<User> users;
    private User userTest = new User("prova", "prova", "Salerno", 43.2, 37);
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
                users.add(new User(username, null, comune, karma, permanenza));
            }
        }

        JSONArray usersJson = new JSONArray();
        Population<Individual> population = new Population<>();
        for (int j = 0; j < 10; j++) {
            Individual individual = new Individual();
            for (int i = 0; i < 5; i++) {

                User user = users.get(r.nextInt(users.size() - 1));
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
        Population<Individual> archive = new Population<>();
        int generationsWithoutImprovement = 0;
        int i;
        int probability = 70;
        long startTime = ZonedDateTime.now().toInstant().toEpochMilli();
        for (i = 0; i < 25; i++) {

            Population<Individual> selectedPopulation = new RouletteWheel(startPopulation).run(location);
            if (selectedPopulation.isEmpty()) {
                return "popolazione vuota";
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
                return "popolazione vuota";
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
                return "popolazione vuota";
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
            System.out.println(currentTime - startTime);
            if (currentTime - startTime >= 150000) {
                break;
            }
        }

        if (bestPopulation.getAverageFitness(location) <= archive.getAverageFitness(location)) {
            bestPopulation = archive;
        }

        System.out.println("Fine del processo. Fitness popolazione: " + bestPopulation.getAverageFitness(location));
        return "Iterazione " + i + " Individuo migliore: " + bestPopulation.getBestIndividual(location).toString();
    }

    @PostMapping("/api/login")
    public String doLogin(@RequestBody MultiValueMap<String, String> body) {
        String username = body.get("username").get(0);
        String password = body.get("password").get(0);

        if (username.equals(userTest.getUserName()) && password.equals(userTest.getPassword())) {
            return "OK";
        } else {
            return "NO";
        }
    }
}