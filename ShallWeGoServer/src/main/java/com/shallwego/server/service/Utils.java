package com.shallwego.server.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.logic.entities.Report;
import com.shallwego.server.logic.entities.Stop;
import com.shallwego.server.logic.entities.User;
import com.shallwego.server.logic.service.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

    public static HashMap<String, Location> alreadyKnownLocations = new HashMap<>();
    public static HashMap<String, List<String>> province = null;
    public static HashMap<Integer, Integer> stopCrowding = null;

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

    public static void setStopCrowding(HashMap<Integer, Integer> incoming) {
        Utils.stopCrowding = incoming;
    }

    public static JsonObject setUpReportJson(Report report) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/YYYY HH:mm");
        JsonObject obj = new JsonObject();
        obj.addProperty("id", report.getId());
        obj.addProperty("user", report.getUser().getUserName());
        JsonArray verifiers = new JsonArray();
        for (User verifier: report.getVerifiers()) {
            verifiers.add(verifier.getUserName());
        }
        obj.add("verifiers", verifiers);
        obj.addProperty("date", formatter.format(report.getDate()));
        obj.addProperty("isVerified", report.isVerified());

        return obj;
    }

    public static Set<User> bestUsersFromPopulation(Population<Individual> population, Location location) throws IOException, ParseException {
        Set<User> bestUsers = new HashSet<>();

        for (Individual individual: population.getIndividuals()) {
            List<User> userList = new ArrayList<>(individual.getUsers());
            userList.sort((user1, user2) -> {
                double user1fitness = 0d;
                double user2fitness = 0d;
                try {
                    user1fitness = getSingleUserFitness(user1, location);
                    user2fitness = getSingleUserFitness(user2, location);

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

            for (int i = userList.size() - 1; i >= 0; i--) {
                if (!bestUsers.contains(userList.get(i))) {
                    bestUsers.add(userList.get(i));
                    break;
                }
            }
        }

        if (bestUsers.size() >= 5) {
            List<User> cutList = new ArrayList<>(bestUsers);
            cutList.sort((user1, user2) -> {
                double user1fitness = 0d;
                double user2fitness = 0d;
                try {
                    user1fitness = getSingleUserFitness(user1, location);
                    user2fitness = getSingleUserFitness(user2, location);

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
            return new HashSet<>(cutList.subList(0, 5));
        }

        return bestUsers;
    }

    private static double getSingleUserFitness(User user, Location location) throws IOException, ParseException {
        double distance = location.distance(user.getComune());
        double distancePartialFitness;
        if (distance == 0) {
            distancePartialFitness = 100 / (distance + 0.3); //avoid division by 0
        } else {
            distancePartialFitness = Math.pow(30 / distance, 2);
        }

        double karmaPartialFitness;

        if (user.getKarma() < 42) {
            karmaPartialFitness = user.getKarma();
        } else {
            karmaPartialFitness = user.getKarma() * 2;
        }

        return (4 * distancePartialFitness) + (2 * karmaPartialFitness) / 2;
    }

    public static String getRoadNameByCoordinates(String latitude, String longitude) throws IOException {
        URL nominatimServer = new URL(IpAddress.GEOCODING_SERVER_ADDRESS + "/search.php?q=" + latitude + " ".replace(" ", "%20") + longitude);
        HttpURLConnection con = (HttpURLConnection) nominatimServer.openConnection();
        con.setRequestMethod("GET");
        String line = "";
        StringBuffer response = new StringBuffer();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        con.disconnect();
        String responseString = response.toString();
        JsonArray responseArray = (JsonArray) JsonParser.parseString(responseString);
        JsonObject object = (JsonObject) responseArray.get(0);
        return object.get("display_name").toString().replace("\"", "");
    }

}
