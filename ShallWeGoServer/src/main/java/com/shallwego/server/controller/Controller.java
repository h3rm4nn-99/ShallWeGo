package com.shallwego.server.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shallwego.server.ga.AlgorithmRunner;
import com.shallwego.server.ga.entities.Individual;
import com.shallwego.server.ga.entities.Population;
import com.shallwego.server.logic.entities.*;
import com.shallwego.server.logic.service.*;
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

    public static List<User> users;

    @PutMapping("/api/putLocation")
    public String printLocation(@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude) {
        System.out.println("Latitudine " + latitude + "\nLongitudine " + longitude);
        return "OKAY\nLatitudine " + latitude + "\nLongitudine " + longitude;
    }

    @GetMapping("/api/createPeople")
    public String createPeople() throws IOException, ParseException {

        users = userRepository.findByProvincia("Salerno");

        if (users.isEmpty()) {
            Utils.populateDbWithRandomUsers(userRepository);
            users = userRepository.findByProvincia("Salerno");
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
        Optional<User> optionalUser = userRepository.findById(username);
        User user = null;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            return "ERR_USER_NOT_FOUND";
        }

        password = DigestUtils.sha512Hex(password);

        if (password.equals(user.getPassword())) {
            return new Gson().toJson(user);
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
        return JSONArray.toJSONString(new ArrayList(Utils.province.keySet()));
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
                obj.addProperty("type", "CompanyReport");
            } else if (report instanceof LineReport) {
                LineReport lineReport = (LineReport) report;
                obj = Utils.setUpReportJson(lineReport);
                obj.addProperty("companyName", lineReport.getLineAffected().getCompany().getName());
                obj.addProperty("lineIdentifier", lineReport.getLineAffected().getIdentifier());
                obj.addProperty("type", "LineReport");
            } else if (report instanceof TemporaryEventReport) {
                TemporaryEventReport temporaryEventReport = (TemporaryEventReport) report;
                obj = Utils.setUpReportJson(temporaryEventReport);
                obj.addProperty("validityStart", temporaryEventReport.getValidityStart().toString());
                obj.addProperty("validityEnd", temporaryEventReport.getValidityEnd().toString());
                List<Line> linesAffected = temporaryEventReport.getLinesAffected();
                JsonArray linesAffectedJson = new JsonArray();
                for (Line line: linesAffected) {
                    JsonObject object = new JsonObject();
                    object.addProperty("lineIdentifier", line.getIdentifier());
                    object.addProperty("companyName", line.getCompany().getName());
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
                obj.addProperty("type", "StopReport");
            }
            array.add(obj);
        }
        return array.toString();
    }
}