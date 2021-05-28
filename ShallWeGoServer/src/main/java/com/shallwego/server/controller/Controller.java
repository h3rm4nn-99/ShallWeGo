package com.shallwego.server.controller;

import com.shallwego.server.ga.AlgorithmRunner;
import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.logic.entities.User;
import com.shallwego.server.logic.service.UserRepository;
import com.shallwego.server.service.Location;
import com.shallwego.server.service.Utils;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.*;
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
            Utils.populateDbWithRandomUsers(repository);
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

    @PostMapping("/api/login")
    public String doLogin(@RequestBody MultiValueMap<String, String> body) {
        String username = body.get("username").get(0);
        String password = body.get("password").get(0);
        Optional<User> optionalUser = repository.findById(username);
        User user = null;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            return "ERR_USER_NOT_FOUND";
        }

        password = DigestUtils.sha512Hex(password);

        if (password.equals(user.getPassword())) {
            return user.toString();
        } else {
            return "ERR_PWD_INCORRECT";
        }
    }
}