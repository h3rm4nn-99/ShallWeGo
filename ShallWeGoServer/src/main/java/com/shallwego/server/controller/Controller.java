package com.shallwego.server.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shallwego.server.ga.AlgorithmRunner;
import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.logic.entities.*;
import com.shallwego.server.logic.service.*;
import com.shallwego.server.service.IpAddress;
import com.shallwego.server.service.Location;
import com.shallwego.server.service.Utils;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@CrossOrigin
public class Controller {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private TemporaryEventReportRepository temporaryEventReportRepository;

    public static List<User> users;

    @PutMapping("/api/putLocation")
    public String printLocation(@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude) {
        System.out.println("Latitudine " + latitude + "\nLongitudine " + longitude);
        return "OKAY\nLatitudine " + latitude + "\nLongitudine " + longitude;
    }

    @GetMapping("/api/createPeople")
    public String createPeople() throws IOException, ParseException {

        users = userRepository.findByProvincia("Salerno");

        if (users.size() <= 1) {
            Utils.populateDbWithRandomUsers(userRepository);
            users = userRepository.findByProvincia("Salerno");
        }

        Random r = new Random();
        Population<Individual> startPopulation = new Population<>();
        for (int j = 0; j < AlgorithmRunner.POPULATION_SIZE; j++) {
            Individual individual = new Individual();
            for (int i = 0; i < AlgorithmRunner.INDIVIDUAL_SIZE; i++) {
                User user = users.get(r.nextInt(users.size() - 1));
                if (individual.getUsers().contains(user)) {
                    i--;
                    continue;
                }
                individual.addUser(user);
            }
            startPopulation.addIndividual(individual);
        }
        Location location = new Location(40.0742524, 15.6235266);
        Population<Individual> bestPopulation = AlgorithmRunner.run(startPopulation, location);
        int i = 0;
        for (Individual individual: bestPopulation.getIndividuals()) {
            System.out.println("Individuo " + i);
            for (User user: individual.getUsers()) {
                System.out.print(user.getComune() + " ");
            }
            System.out.println();
            i++;
        }

        Set<User> bestUsers = Utils.bestUsersFromPopulation(bestPopulation, location);
        return "Utenti migliori: " + bestUsers.toString();
    }

    @PostMapping("/api/login")
    public String doLogin(@RequestBody MultiValueMap<String, String> body) {
        String username = body.get("username").get(0);
        String password = body.get("password").get(0);
        Optional<User> optionalUser = userRepository.findById(username);
        User user = null;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            return "ERR_USER_NOT_FOUND";
        }

        password = DigestUtils.sha512Hex(password);

        if (password.equals(user.getPassword())) {
            JsonObject response = new JsonObject();
            response.addProperty("karma", user.getKarma());

            return response.toString();
        } else {
            return "ERR_PWD_INCORRECT";
        }
    }

    @PostMapping("/api/register")
    public String register(@RequestBody MultiValueMap<String, String> body) {
        String username = body.get("username").get(0);
        String password = body.get("password").get(0);
        String comune = body.get("comune").get(0);
        String provincia = body.get("provincia").get(0);
        if (userRepository.existsById(username)) {
            return "ERR_USER_ALREADY_PRESENT";
        }

        userRepository.save(new User(username, password, comune, provincia, 0.0));
        return "OK";
    }

    @GetMapping("/api/province")
    public String province() {
        return JSONArray.toJSONString(new ArrayList<String>(Utils.province.keySet()));
    }

    @GetMapping("/api/provincia/{provincia}/comuni")
    public String comuniByProvincia(@PathVariable String provincia) {
        return JSONArray.toJSONString(Utils.province.get(provincia));
    }

    @PostMapping("/api/reports/{userId}")
    public String reportsByUser(@PathVariable String userId) {
        List<Report> reports = reportRepository.findByUser(userRepository.findById(userId).get());
        JsonArray array = new JsonArray();
        for (Report report: reports) {
            JsonObject obj = null;
            if (report instanceof CompanyReport) {
                CompanyReport companyReport = (CompanyReport) report;
                obj = Utils.setUpReportJson(companyReport);
                Company company = companyReport.getCompany();
                obj.addProperty("companyName", company.getName());
                obj.addProperty("companyWebsite", company.getWebsite());
                obj.addProperty("type", "CompanyReport");
            } else if (report instanceof LineReport) {
                LineReport lineReport = (LineReport) report;
                obj = Utils.setUpReportJson(lineReport);
                obj.addProperty("companyName", lineReport.getLineAffected().getCompany().getName());
                obj.addProperty("lineIdentifier", lineReport.getLineAffected().getIdentifier());
                obj.addProperty("destination", lineReport.getLineAffected().getDestination());
                obj.addProperty("type", "LineReport");
            } else if (report instanceof TemporaryEventReport) {
                TemporaryEventReport temporaryEventReport = (TemporaryEventReport) report;
                obj = Utils.setUpReportJson(temporaryEventReport);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/YYYY HH:mm");
                obj.addProperty("validityStart", formatter.format(temporaryEventReport.getValidityStart()));
                obj.addProperty("validityEnd", formatter.format(temporaryEventReport.getValidityEnd()));
                List<Line> linesAffected = temporaryEventReport.getLinesAffectedEvent();
                JsonArray linesAffectedJson = new JsonArray();
                for (Line line: linesAffected) {
                    JsonObject object = new JsonObject();
                    object.addProperty("lineIdentifier", line.getIdentifier());
                    object.addProperty("companyName", line.getCompany().getName());
                    object.addProperty("destination", line.getDestination());
                    linesAffectedJson.add(object);
                }
                obj.add("linesAffected", linesAffectedJson);
                obj.addProperty("latitude", temporaryEventReport.getLatitude());
                obj.addProperty("longitude", temporaryEventReport.getLongitude());
                obj.addProperty("description", temporaryEventReport.getDescription());
                obj.addProperty("type", "TemporaryEventReport");
            } else if (report instanceof StopReport) {
                StopReport stopReport = (StopReport) report;
                obj = Utils.setUpReportJson(stopReport);
                obj.addProperty("stopId", stopReport.getStopReported().getId());
                obj.addProperty("stopName", stopReport.getStopReported().getName());
                obj.addProperty("latitude", stopReport.getStopReported().getLatitude());
                obj.addProperty("longitude", stopReport.getStopReported().getLongitude());
                obj.addProperty("type", "StopReport");
            }
            array.add(obj);
        }
        return array.toString();
    }

    @Transactional
    @PutMapping("/api/addToFavorites/{username}/{stopId}")
    public boolean addFavoriteStop(@PathVariable String username, @PathVariable String stopId) {
        Stop stop = stopRepository.findById(Integer.parseInt(stopId)).get();
        User user = userRepository.findById(username).get();
        user.addPreferredStop(stop);

        return user.getPreferredStops().contains(stop);
    }

    @Transactional
    @DeleteMapping("/api/removeFromFavorites/{username}/{stopId}")
    public boolean removeFavoriteStop(@PathVariable String username, @PathVariable String stopId) {
        Stop stop = stopRepository.findById(Integer.parseInt(stopId)).get();
        User user = userRepository.findById(username).get();
        if (user.getPreferredStops().contains(stop)) {
            user.removePreferredStop(stop);
        }
        return user.getPreferredStops().contains(stop);
    }

    @GetMapping("/api/isFavorite/{username}/{stopId}")
    public boolean isFavorite(@PathVariable String username, @PathVariable String stopId) {
        Stop stop = stopRepository.findById(Integer.parseInt(stopId)).get();
        User user = userRepository.findById(username).get();

        return user.getPreferredStops().contains(stop);
    }

    @GetMapping("/api/getFavorites/{username}")
    public String getFavoriteStopsByUser(@PathVariable String username) {
        User user = userRepository.findById(username).get();
        List<Stop> favoriteStops = user.getPreferredStops();
        JsonArray stopArray = new JsonArray();

        for (Stop stop: favoriteStops) {
            JsonObject stopJsonObject = new JsonObject();
            stopJsonObject.addProperty("stopId", stop.getId());
            stopJsonObject.addProperty("stopName", stop.getName());
            stopJsonObject.addProperty("latitude", stop.getLatitude());
            stopJsonObject.addProperty("longitude", stop.getLongitude());
            stopArray.add(stopJsonObject);
        }

        return stopArray.toString();
    }

    @Transactional
    @GetMapping("/api/getStopDetails/{stopId}")
    public String getStopDetails(@PathVariable String stopId) {
        int intStopId = Integer.valueOf(stopId);
        Stop stop = stopRepository.findById(intStopId).get();
        List<Line> stopLines = stop.getLines();
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("stopName", stop.getName());
        responseJson.addProperty("hasShelter", stop.getHasShelter());
        responseJson.addProperty("crowding", Utils.stopCrowding.get(stop.getId()));
        responseJson.addProperty("hasTimeTables", stop.getHasTimeTables());
        responseJson.addProperty("hasStopSign", stop.getHasStopSign());
        responseJson.addProperty("latitude", stop.getLatitude());
        responseJson.addProperty("longitude", stop.getLongitude());
        JsonArray linesJson = new JsonArray();
        for (Line l: stopLines) {
            JsonObject lineJsonObject = new JsonObject();
            lineJsonObject.addProperty("lineIdentifier", l.getIdentifier());
            lineJsonObject.addProperty("companyName", l.getCompany().getName());
            lineJsonObject.addProperty("destination", l.getDestination());

            linesJson.add(lineJsonObject);
        }

        responseJson.add("lines", linesJson);
        return responseJson.toString();
    }

    @GetMapping("/api/getStopCrowding/{stopId}")
    public String getStopCrowding(@PathVariable int stopId) {
        return String.valueOf(Utils.stopCrowding.get(stopId));
    }

    @PutMapping("/api/updateStopCrowding/{stopId}/{newValue}")
    public String updateStopCrowding(@PathVariable String stopId, @PathVariable String newValue) {
        Utils.stopCrowding.put(Integer.valueOf(stopId), Integer.valueOf(newValue));
        return String.valueOf(Utils.stopCrowding.get(Integer.valueOf(stopId)));
    }

    @GetMapping("/api/getAlertsNearby/{coordinates}")
    public String getAlertsNearby(@PathVariable String coordinates) throws IOException {
        String[] splitCoordinates = coordinates.split(",");
        Location location = new Location(Double.parseDouble(splitCoordinates[0]), Double.parseDouble(splitCoordinates[1]));
        ArrayList<TemporaryEventReport> relevantAlerts = new ArrayList<>();
        List<TemporaryEventReport> allAlerts = temporaryEventReportRepository.findAll();
        for (TemporaryEventReport temporaryEventReport: allAlerts) {
            Location alertLocation = new Location(Double.parseDouble(temporaryEventReport.getLatitude()), Double.parseDouble(temporaryEventReport.getLongitude()));
            if (alertLocation.distance(location) <= 10000) {
                relevantAlerts.add(temporaryEventReport);
            }
        }
        JsonArray returnArray = new JsonArray();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (TemporaryEventReport target: relevantAlerts) {
            JsonObject targetJson = new JsonObject();
            targetJson.addProperty("id", target.getId());
            targetJson.addProperty("type", target.getEventType());
            targetJson.addProperty("place", Utils.getRoadNameByCoordinates(target.getLatitude(), target.getLongitude()));
            targetJson.addProperty("timeValid", formatter.format(target.getValidityStart()) + " - " + formatter.format(target.getValidityEnd()));
            targetJson.addProperty("latitude", target.getLatitude());
            targetJson.addProperty("longitude", target.getLongitude());

            List<Line> linesAffected = target.getLinesAffectedEvent();
            StringBuilder linesAffectedPreview = new StringBuilder();
            linesAffected.forEach((line) -> linesAffectedPreview.append(line.getIdentifier()).append(linesAffected.size() == 1? "" : ", "));
            targetJson.addProperty("linesAffectedPreview", linesAffectedPreview.toString());
            returnArray.add(targetJson);
        }
        return returnArray.toString();
    }
}