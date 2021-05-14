package com.shallwego.server.controller;

import com.shallwego.server.ga.AlgorithmRunner;
import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.logic.entities.User;
import com.shallwego.server.service.Location;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

        Population<Individual> startPopulation = new Population<>();
        for (int j = 0; j < 10; j++) {
            Individual individual = new Individual();
            for (int i = 0; i < 5; i++) {
                User user = users.get(r.nextInt(users.size() - 1));
                individual.addUser(user);
            }
            startPopulation.addIndividual(individual);
        }
        Location location = new Location(40.7415603, 14.6715039);
        Population<Individual> bestPopulation = AlgorithmRunner.run(startPopulation, location);

        return "Individuo migliore: " + bestPopulation.getBestIndividual(location).toString();
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