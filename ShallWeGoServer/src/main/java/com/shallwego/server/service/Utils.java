package com.shallwego.server.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.ga.entities.UserGA;
import com.shallwego.server.logic.entities.Report;
import com.shallwego.server.logic.entities.Stop;
import com.shallwego.server.logic.entities.User;
import com.shallwego.server.logic.service.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class Utils {

    public static HashMap<String, Location> alreadyKnownLocations = new HashMap<>();
    public static HashMap<String, Set<String>> province = null;
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

    public static void setProvince(HashMap<String, Set<String>> incoming) {
        Utils.province = incoming;
    }

    public static void setStopCrowding(HashMap<Integer, Integer> incoming) {
        Utils.stopCrowding = incoming;
    }

    public static JsonObject setUpReportJson(Report report) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        JsonObject obj = new JsonObject();
        obj.addProperty("id", report.getId());
        obj.addProperty("user", report.getUser().getUserName());
        JsonArray verifiers = new JsonArray();
        for (User verifier: report.getVerifiers()) {
            verifiers.add(verifier.getUserName());
        }
        obj.add("verifiers", verifiers);
        obj.addProperty("date", formatter.format(report.getDate()));
        return obj;
    }

    public static Location getCoordinatesByComune(String comune, String provincia) throws IOException {

        Location result = Utils.alreadyKnownLocations.get(comune);
        if (result != null) {
            return result;
        } else {
            double latitude = 0.0d;
            double longitude = 0.0d;
            URL nominatimServer = new URL(IpAddress.GEOCODING_SERVER_ADDRESS + "/search.php?q=" + comune.replace(" ", "%20") + ",%20" + provincia.replace(" ", "%20"));
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
            JsonArray array = JsonParser.parseString(responseString).getAsJsonArray();
            if (array.isEmpty()) {
                throw new IllegalStateException("Comune non trovato!");
            }

            JsonObject obj = (JsonObject) array.get(0);
            latitude = Double.parseDouble(obj.get("lat").getAsString());
            longitude = Double.parseDouble(obj.get("lon").getAsString());
            Location location = new Location(latitude, longitude);
            Utils.alreadyKnownLocations.put(comune, location);
            return location;
        }
    }

    public static String getRoadNameByCoordinates(String latitude, String longitude) throws IOException {
        URL nominatimServer = new URL(IpAddress.GEOCODING_SERVER_ADDRESS + "/search.php?q=" + latitude + "%20" + longitude);
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

    public static List<User> getByProvincia(double latitude, double longitude, UserRepository repository) throws IllegalStateException, IOException {
        URL nominatimServer = new URL(IpAddress.GEOCODING_SERVER_ADDRESS + "/reverse.php?lat=" + latitude + "&lon=" + longitude + "&format=json");
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
        JsonObject object = (JsonObject) JsonParser.parseString(responseString);
        if (object.has("error"))    throw new IllegalStateException("Errore di Geocoding!");
        System.out.println(object);
        String provincia = "";
        if (!object.get("address").getAsJsonObject().has("county") && object.get("address").getAsJsonObject().get("state").toString().replace("\"", "").equalsIgnoreCase("Valle d'Aosta/Vallée d'Aoste")) {
            provincia = "Valle d'Aosta";
        } else if (object.get("address").getAsJsonObject().get("county").toString().replace("\"", "").equalsIgnoreCase("Bolzano - Bozen")) {
            provincia = "Bolzano";
        } else {
            provincia = object.get("address").getAsJsonObject().get("county").toString().replace("\"", "");
        }

        return repository.findByProvincia(provincia);
    }

}
