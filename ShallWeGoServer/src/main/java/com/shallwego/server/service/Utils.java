package com.shallwego.server.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.logic.entities.Report;
import com.shallwego.server.logic.entities.User;
import com.shallwego.server.logic.service.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Utils {

    public static HashMap<String, Location> alreadyKnownLocations = new HashMap<>();
    public static HashMap<String, List<String>> province = null;

    public static HashMap<Individual, Double> getNormalizedFitness(Population population, Location location) throws IOException, ParseException {
        HashMap<Individual, Double> normalizedFitness = new HashMap<>();
        HashMap<Individual, Double> usersWithFitness = population.getPopulationFitness(location);
        ArrayList<Double> fitnessValues = new ArrayList<>();

        for (Individual individual: usersWithFitness.keySet()) {
            fitnessValues.add(usersWithFitness.get(individual));
        }

        double sumOfFitnessValues = fitnessValues.stream().mapToDouble(value -> value).sum(); //Please forgive me

        for (Individual user: usersWithFitness.keySet()) {
            normalizedFitness.put(user, (usersWithFitness.get(user) / sumOfFitnessValues));
        }
        return normalizedFitness;
    }

    public static void populateDbWithRandomUsers(UserRepository repository) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        Random r = new Random();
        JSONArray array = (JSONArray) parser.parse(new FileReader("comuni.json"));
        Iterator<JSONObject> iterator = array.iterator();

        int i = 0;
        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();
            JSONObject provinciaCurrent = (JSONObject) obj.get("provincia");
            String provinciaString = (String) provinciaCurrent.get("nome");
            String username = "suspicioususer" + i;
            double karma = r.nextDouble() + r.nextInt(55);
            String comune = (String) obj.get("nome");
            System.out.println(comune);
            i++;
            repository.save(new User(username, "test", comune, provinciaString, karma));
        }
    }

    public static void setProvince(HashMap<String, List<String>> incoming) {
        Utils.province = incoming;
    }

    public static JsonObject setUpReportJson(Report report) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", report.getId());
        obj.addProperty("user", report.getUser().getUserName());
        JsonArray verifiers = new JsonArray();
        for (User verifier: report.getVerifiers()) {
            verifiers.add(verifier.getUserName());
        }
        obj.add("verifiers", verifiers);
        obj.addProperty("date", report.getDate());
        obj.addProperty("isVerified", report.isVerified());

        return obj;
    }

}
