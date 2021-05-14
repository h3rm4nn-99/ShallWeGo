package com.shallwego.server.controller;

import com.shallwego.server.ga.AlgorithmRunner;
import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.logic.entities.User;
import com.shallwego.server.logic.service.UserRepository;
import com.shallwego.server.service.Location;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class Controller {

    @Autowired
    private UserRepository repository;

    public static List<User> users;

    @PutMapping("/api/putLocation")
    public String printLocation(@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude) {
        System.out.println("Latitudine " + latitude + "\nLongitudine " + longitude);
        return "OKAY\nLatitudine " + latitude + "\nLongitudine " + longitude;
    }

    @GetMapping("/api/createPeople")
    public String createPeople() throws IOException, ParseException {

        users = repository.findByProvincia("Salerno");

        if (users.isEmpty()) {
            JSONParser parser = new JSONParser();
            Random r = new Random();
            JSONArray array = (JSONArray) parser.parse(new FileReader("comuni.json"));
            Iterator<JSONObject> iterator = array.iterator();

            while (iterator.hasNext()) {
                JSONObject obj = iterator.next();
                JSONObject provinciaCurrent = (JSONObject) obj.get("provincia");
                String provinciaString = (String) provinciaCurrent.get("nome");
                byte[] byteArray = new byte[11];
                new Random().nextBytes(byteArray);
                String username = new String(byteArray, StandardCharsets.UTF_8);
                double karma = r.nextDouble() + r.nextInt(55);
                int permanenza = r.nextInt(365);
                String comune = (String) obj.get("nome");
                System.out.println(comune);
                repository.save(new User(username, "test", comune, provinciaString, karma, permanenza));
            }

            users = repository.findByProvincia("Salerno");
        }

        Random r = new Random();
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

    @PostMapping("/api/loginTest")
    public String doLogin(@RequestBody MultiValueMap<String, String> body) {
        System.out.println("Here I am!");
        User userTest;
        Optional<User> optionalUserTest = repository.findById("prova");
        if (optionalUserTest.isPresent()) {
            userTest = optionalUserTest.get();
        } else {
            userTest = new User("prova", "prova", "Salerno", "Salerno", 43.2, 37);
            repository.save(userTest);
        }

        String username = body.get("username").get(0);
        String password = body.get("password").get(0);

        if (username.equals(userTest.getUserName()) && DigestUtils.sha512Hex(password).equals(userTest.getPassword())) {
            System.out.println("yea");
            return "OK";
        } else {
            System.out.println("nay");
            return "NO";
        }
    }
}