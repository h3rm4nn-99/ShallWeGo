package com.shallwego.server.controller;

import com.google.gson.*;
import com.shallwego.server.ga.AlgorithmRunner;
import com.shallwego.server.logic.entities.*;
import com.shallwego.server.logic.service.*;
import com.shallwego.server.rides.Ride;
import com.shallwego.server.rides.RideManager;
import com.shallwego.server.service.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.io.*;
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

    @Autowired
    private DestinationsByStopAndLineRepository destinationsByStopAndLineRepository;

    @Autowired
    private DestinationsByReportAndLineRepository destinationsByReportAndLineRepository;

    @Autowired
    private RideManager rideManager;

    @Autowired
    private Pool pool;


    public static List<User> users;

    public Controller() {
    }

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


        Location location = new Location(40.6803601, 14.7594542);
        Set<User> bestUsers = AlgorithmRunner.buildPopulation(users, location).run();

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
        JsonArray comuniByProvincia = new JsonArray();
        Utils.province.get(provincia).forEach(comuniByProvincia::add);
        return comuniByProvincia.toString();
    }

    @PostMapping("/api/reports/{userId}")
    public String reportsByUser(@PathVariable String userId) {
        User user = userRepository.findById(userId).get();
        List<Report> reports = reportRepository.findByUser(user);
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
                obj.addProperty("type", "LineReport");
            } else if (report instanceof TemporaryEventReport) {
                TemporaryEventReport temporaryEventReport = (TemporaryEventReport) report;
                obj = Utils.setUpReportJson(temporaryEventReport);
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                obj.addProperty("validityStart", formatter.format(temporaryEventReport.getValidityStart()));
                if (temporaryEventReport.getValidityEnd() != null) {
                    obj.addProperty("validityEnd", formatter.format(temporaryEventReport.getValidityEnd()));
                }

                List<Line> linesAffected = temporaryEventReport.getLinesAffectedEvent();
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
                obj.addProperty("eventType", temporaryEventReport.getEventType());
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
            List<DestinationsByStopAndLine> destinationsList = destinationsByStopAndLineRepository.findByTargetLineAndTargetStop(stop, l);
            DestinationsByStopAndLine destinationsObject = destinationsList.get(0);
            List<String> destinations = destinationsObject.getTargetDestinations();
            JsonArray destinationsJsonArray = new JsonArray();
            for (String destination: destinations) {
                destinationsJsonArray.add(destination);
            }
            lineJsonObject.add("destinations", destinationsJsonArray);

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
            targetJson.addProperty("timeValid", formatter.format(target.getValidityStart()) + " - " + (target.getValidityEnd() != null? formatter.format(target.getValidityEnd()) : "in corso"));
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

    @GetMapping("/api/eventById/{eventId}")
    public String eventById(@PathVariable String eventId) throws IOException {
        TemporaryEventReport target = temporaryEventReportRepository.findById(Integer.parseInt(eventId)).get();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        JsonObject targetJson = new JsonObject();
        targetJson.addProperty("id", target.getId());
        targetJson.addProperty("type", target.getEventType());
        targetJson.addProperty("place", Utils.getRoadNameByCoordinates(target.getLatitude(), target.getLongitude()));
        targetJson.addProperty("validityStart", formatter.format(target.getValidityStart()));
        if (target.getValidityEnd() != null) {
            targetJson.addProperty("validityEnd", formatter.format(target.getValidityEnd()));
        }
        targetJson.addProperty("latitude", target.getLatitude());
        targetJson.addProperty("longitude", target.getLongitude());
        targetJson.addProperty("description", target.getDescription());

        List<Line> linesAffected = target.getLinesAffectedEvent();
        JsonArray linesJson = new JsonArray();

        for (Line line: linesAffected) {
            JsonObject lineJsonObject = new JsonObject();
            lineJsonObject.addProperty("lineIdentifier", line.getIdentifier());
            lineJsonObject.addProperty("companyName", line.getCompany().getName());

            linesJson.add(lineJsonObject);
        }

        targetJson.add("linesAffected", linesJson);

        return targetJson.toString();
    }

    @GetMapping("/api/getLineDetails/{lineIdentifier}/{companyName}")
    public String getLineDetails(@PathVariable String lineIdentifier, @PathVariable String companyName) {
        Line line = lineRepository.findById(new LineCompositeKey(lineIdentifier, companyName)).get();
        JsonObject result = new JsonObject();
        result.addProperty("lineIdentifier", line.getIdentifier());
        result.addProperty("companyName", line.getCompany().getName());
        JsonArray routesJson = new JsonArray();
        List<Route> routes = line.getPaths();
        for (Route route: routes) {
            JsonObject object = new JsonObject();
            object.addProperty("pathname", route.getPathname());
            JsonArray stopOrders = new JsonArray();
            for (Integer stopId: route.getStopIds()) {
                JsonObject stopDetails = new JsonObject();
                Stop s = stopRepository.findById(stopId).get();
                stopDetails.addProperty("name", s.getName());
                stopDetails.addProperty("stopId", stopId);
                stopDetails.addProperty("latitude", s.getLatitude());
                stopDetails.addProperty("longitude", s.getLongitude());
                stopOrders.add(stopDetails);
            }
            object.add("stops", stopOrders);
            routesJson.add(object);
        }
        result.add("routes", routesJson);

        return result.toString();
    }

    @GetMapping("/api/getCompanies")
    public String getCompanies() {
        List<Company> companies = companyRepository.findAll();
        JsonArray companyArray = new JsonArray();
        companies.forEach((company) -> {
            companyArray.add(company.getName());
        });

        return companyArray.toString();
    }

    @GetMapping("/api/getCompanyLines/{companyName}")
    public String getComapnyLines(@PathVariable String companyName) {
        Company company = companyRepository.findById(companyName).get();
        JsonArray linesArray = new JsonArray();
        company.getLinee().forEach((line) -> {
            linesArray.add(line.getIdentifier());
        });

        return linesArray.toString();
    }

    @GetMapping("/api/getLineDestinations/{companyName}/{identifier}")
    public String getLineDestinations(@PathVariable String companyName, @PathVariable String identifier) {
        Company company = companyRepository.findById(companyName).get();
        Line line = lineRepository.findById(new LineCompositeKey(identifier, companyName)).get();
        JsonArray jsonArray = new JsonArray();
        line.getDestinations().forEach(jsonArray::add);
        return jsonArray.toString();
    }

    @PutMapping("/api/newStopReport/{userName}")
    public String newStopReport(@PathVariable String userName, @RequestBody String jsonBody) throws IOException, ParseException {
        JsonObject bodyObject = (JsonObject) JsonParser.parseString(jsonBody);
        StopReport report = new StopReport();
        Stop stop = Stop.newInstance();
        report.setDate(new Date());
        report.setUser(userRepository.findById(userName).get());
        report.setStopReported(stop);
        stop.setName(bodyObject.get("stopName").toString().replace("\"", ""));
        JsonArray array = bodyObject.getAsJsonArray("lines");
        HashMap<Line, List<String>> stopDestinations = new HashMap<>();
        for (JsonElement line: array) {
            JsonObject lineObject = (JsonObject) line;
            String identifier = lineObject.get("lineIdentifier").toString().replace("\"", "");
            Line lineFromDb = lineRepository.findById(new LineCompositeKey(identifier, lineObject.get("companyName").getAsString())).get();
            stop.addLine(lineFromDb);
            ArrayList<String> destinations = new ArrayList<>();
            JsonArray lineDestinations = lineObject.getAsJsonArray("destinations");
            lineDestinations.forEach((destination) -> {
                destinations.add(destination.toString().replace("\"", ""));
            });
            stopDestinations.put(lineFromDb, destinations);
        }
        double latitude = bodyObject.get("latitude").getAsDouble();
        double longitude = bodyObject.get("longitude").getAsDouble();
        stop.setLatitude(latitude);
        stop.setLongitude(longitude);
        stop.setHasShelter(bodyObject.get("hasShelter").getAsBoolean());
        stop.setHasStopSign(bodyObject.get("hasStopSign").getAsBoolean());
        stop.setHasTimeTables(bodyObject.get("hasTimeTables").getAsBoolean());
        List<User> candidates = null;
        try {
            candidates = Utils.getByProvincia(latitude, longitude, userRepository);
        } catch (IOException e) {
            return "GEOCODING_ERROR";
        }
        Set<User> verifiers = AlgorithmRunner.buildPopulation(candidates, new Location(latitude, longitude)).run();
        pool.addPendingReports(report, verifiers, stopDestinations);
        System.out.println(verifiers.toString());
        return new Gson().toJson(verifiers.stream().map(User::getComune).toArray(String[]::new));
    }

    @PutMapping("/api/newLineReport/{userName}")
    @Transactional
    public String newLineReport(@PathVariable String userName, @RequestBody String jsonBody) throws IOException, ParseException {
        JsonObject objectBody = (JsonObject) JsonParser.parseString(jsonBody);
        LineReport lineReport = new LineReport();
        lineReport.setDate(new Date());
        lineReport.setUser(userRepository.findById(userName).get());
        lineReport.setVerified(false);
        Line line = new Line();
        line.setCompany(companyRepository.findById(objectBody.get("company").getAsString()).get());
        boolean hasIdentifier = objectBody.get("hasIdentifier").getAsBoolean();
        String identifier = "";
        if (objectBody.get("hasIdentifier").getAsBoolean()) {
            identifier = objectBody.get("identifier").getAsString();
        } else {
            if (objectBody.get("comuneOrigin").getAsString().equals(objectBody.get("comuneDestination").getAsString())) {
                identifier = objectBody.get("detailsOrigin").getAsString().substring(0, 3).toUpperCase().trim() + " - " + objectBody.get("detailsDestination").getAsString().substring(0, 3).toUpperCase().trim();
            } else {
                identifier = objectBody.get("comuneOrigin").getAsString().substring(0, 3).toUpperCase().trim() + " - " + objectBody.get("comuneDestination").getAsString().substring(0, 3).toUpperCase().trim();
            }
        }
        line.setIdentifier(identifier);
        ArrayList<String> destinations = new ArrayList<>();
        objectBody.get("destinations").getAsJsonArray().forEach((destination) -> {
            String destinationString = ((JsonObject) destination).get("comune") + " " + ((JsonObject) destination).get("name");
            destinations.add(destinationString);
        });
        line.setDestinations(destinations);
        Set<User> verifiers = new HashSet<>();
        for (JsonElement object: objectBody.get("destinations").getAsJsonArray()) {
            JsonObject destinationObject = (JsonObject) object;
            Location location = Utils.getCoordinatesByComune(destinationObject.get("comune").getAsString(), destinationObject.get("provincia").getAsString());
            List<User> candidates = userRepository.findByProvincia(destinationObject.get("provincia").getAsString());
            verifiers.addAll(AlgorithmRunner.buildPopulation(candidates, location).run());
        }
        lineReport.setLineAffected(line);
        pool.addPendingReports(lineReport, verifiers);
        System.out.println(verifiers.toString());
        return new Gson().toJson(verifiers.stream().map(User::getComune).toArray(String[]::new));
    }

    @Transactional
    @PutMapping("/api/newCompanyReport/{userName}")
    public String newCompanyReport(@RequestBody String reportBody, @PathVariable String userName) throws IOException, ParseException {
        JsonObject object = (JsonObject) JsonParser.parseString(reportBody);
        Company company = new Company();
        company.setName(object.get("companyName").getAsString());
        company.setWebsite(object.get("website").getAsString());
        CompanyReport report = new CompanyReport();
        report.setCompany(company);
        report.setUser(userRepository.findById(userName).get());
        report.setDate(new Date());
        double latitude = object.get("latitude").getAsDouble();
        double longitude = object.get("longitude").getAsDouble();
        Location location = new Location(latitude, longitude);
        List<User> candidates = Utils.getByProvincia(latitude, longitude, userRepository);
        Set<User> verifiers = AlgorithmRunner.buildPopulation(candidates, location).run();
        pool.addPendingReports(report, verifiers);
        System.out.println(verifiers.toString());
        return new Gson().toJson(verifiers.stream().map(User::getComune).toArray(String[]::new));
    }

    @Transactional
    @PutMapping("/api/newTemporaryEventReport/{userName}")
    public String newTemporaryEventReport(@RequestBody String reportBody, @PathVariable String userName) throws java.text.ParseException {
        JsonObject object = (JsonObject) JsonParser.parseString(reportBody);
        TemporaryEventReport report = new TemporaryEventReport();
        report.setDate(new Date());
        report.setUser(userRepository.findById(userName).get());
        report.setSource(object.get("source").getAsString());
        report.setDescription(object.get("description").getAsString());
        report.setEventType(object.get("type").getAsString());
        report.setLatitude(object.get("latitude").getAsString());
        report.setLongitude(object.get("longitude").getAsString());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        report.setValidityStart(formatter.parse(object.get("start").getAsString()));

        if (object.has("end")) {
            report.setValidityEnd(formatter.parse(object.get("end").getAsString()));
        }

        JsonArray array = object.getAsJsonArray("linesAffected");
        for (JsonElement element: array) {
            JsonObject lineObject = (JsonObject) element;
            Line line = lineRepository.findById(new LineCompositeKey(lineObject.get("lineIdentifier").getAsString(), lineObject.get("companyName").getAsString())).get();
            report.addAffectedLine(line);
            line.addTemporaryEvent(report);
            DestinationsByReportAndLine destinationsByReportAndLine = new DestinationsByReportAndLine();
            destinationsByReportAndLine.setTargetReport(report);
            destinationsByReportAndLine.setTargetLine(line);
            HashSet<String> destinations = new HashSet<>();
            lineObject.get("destinations").getAsJsonArray().forEach((destination) -> {
                destinations.add(destination.getAsString());
            });

            destinationsByReportAndLine.setTargetDestinations(destinations);
            report.getDestinationsByReport().add(destinationsByReportAndLine);
            line.getDestinationsByLineReport().add(destinationsByReportAndLine);
            destinationsByReportAndLineRepository.save(destinationsByReportAndLine);
        }

        temporaryEventReportRepository.saveAndFlush(report);

        return "OK";
    }

    @PutMapping("/api/verifyReport/{username}/{pendingId}/{vote}")
    public String verifyReport(@PathVariable String username, @PathVariable Integer pendingId, @PathVariable Integer vote) {
        User user = userRepository.findById(username).get();
        return String.valueOf(pool.verifyReport(pendingId, user, vote));
    }

    @GetMapping("/api/getReportDetails/{id}/{userName}")
    public String getAssignedReports(@PathVariable String id, @PathVariable String userName) {
        PendingReport report = pool.findById(Integer.parseInt(id));
            JsonObject object = new JsonObject();
            object.addProperty("sumOfVotes", report.getSumOfVotes());
            object.addProperty("userVote", report.getUserVote(userRepository.findById(userName).get()));
            object.addProperty("date", new SimpleDateFormat("dd/MM/yyyy").format(report.getReport().getDate()));
            if (report.getReport() instanceof CompanyReport) {
                CompanyReport companyReport = (CompanyReport) report.getReport();
                object.addProperty("companyName", companyReport.getCompany().getName());
                object.addProperty("companyWebSite", companyReport.getCompany().getWebsite() == null? "" : companyReport.getCompany().getWebsite());
            } else if (report.getReport() instanceof LineReport) {
                LineReport lineReport = (LineReport) report.getReport();
                object.addProperty("companyName", lineReport.getLineAffected().getCompany().getName());
                object.addProperty("lineIdentifier", lineReport.getLineAffected().getIdentifier());
                JsonArray destinations = new JsonArray();
                lineReport.getLineAffected().getDestinations().forEach(destinations::add);
                object.add("destinations", destinations);
            } else if (report.getReport() instanceof StopReport) {
                PendingStopReport pendingReport = (PendingStopReport) report;
                StopReport stopReport = (StopReport) report.getReport();
                object.addProperty("stopName", stopReport.getStopReported().getName());
                JsonArray destinationsByLine = new JsonArray();

                for (Line line: pendingReport.getDestinations().keySet()) {
                    JsonObject lineWithDestinations = new JsonObject();
                    lineWithDestinations.addProperty("companyName", line.getCompany().getName());
                    JsonArray destinations = new JsonArray();
                    pendingReport.getDestinations().get(line).forEach(destinations::add);
                    lineWithDestinations.add("destinations", destinations);
                    lineWithDestinations.addProperty("lineIdentifier", line.getIdentifier());
                    destinationsByLine.add(lineWithDestinations);
                }
                object.add("reachableFromThisStop", destinationsByLine);
                object.addProperty("hasShelter", stopReport.getStopReported().getHasShelter());
                object.addProperty("hasTimeTables", stopReport.getStopReported().getHasTimeTables());
                object.addProperty("hasStopSign", stopReport.getStopReported().getHasTimeTables());
            }

        return object.toString();
    }

    @PostMapping("/api/pendingReports/{userId}")
    public String pendingReportsByUser(@PathVariable String userId) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        User user = userRepository.findById(userId).get();
        List<PendingReport> reports = pool.reportsByAssignedUser(user);
        JsonArray array = new JsonArray();
        for (PendingReport report: reports) {
            JsonObject obj = new JsonObject();
            obj.addProperty("date", formatter.format(report.getReport().getDate()));
            obj.addProperty("pendingId", report.getId());
            if (report.getReport() instanceof CompanyReport) {
                CompanyReport companyReport = (CompanyReport) report.getReport();
                Company company = companyReport.getCompany();
                obj.addProperty("companyName", company.getName());
                obj.addProperty("companyWebsite", company.getWebsite());
                obj.addProperty("type", "CompanyReport");
            } else if (report.getReport() instanceof LineReport) {
                LineReport lineReport = (LineReport) report.getReport();
                obj.addProperty("companyName", lineReport.getLineAffected().getCompany().getName());
                obj.addProperty("lineIdentifier", lineReport.getLineAffected().getIdentifier());
                obj.addProperty("type", "LineReport");
            } else if (report.getReport() instanceof StopReport) {
                StopReport stopReport = (StopReport) report.getReport();
                obj.addProperty("stopName", stopReport.getStopReported().getName());
                obj.addProperty("latitude", stopReport.getStopReported().getLatitude());
                obj.addProperty("longitude", stopReport.getStopReported().getLongitude());
                obj.addProperty("type", "StopReport");
            }
            array.add(obj);
        }
        return array.toString();
    }

    @PostMapping("/api/initRide")
    public String initRide(@RequestBody String rideData) {
        JsonObject object = (JsonObject) JsonParser.parseString(rideData);
        String lineIdentifier = object.get("lineIdentifier").getAsString();
        String companyName = object.get("companyName").getAsString();
        String destination = object.get("destination").getAsString();
        boolean hasAirConditioning = object.get("hasAirConditioning").getAsBoolean();
        int crowding = object.get("crowding").getAsInt();
        ArrayList<String> notes = new ArrayList<>();
        JsonArray notesJson = object.get("notes").getAsJsonArray();
        notesJson.forEach((jsonElement -> {
            notes.add(jsonElement.toString());
        }));

        double latitude = object.get("latitude").getAsDouble();
        double longitude = object.get("longitude").getAsDouble();
        Line line = lineRepository.findById(new LineCompositeKey(lineIdentifier, companyName)).get();
        Ride ride = new Ride(line, destination, new Location(latitude, longitude));
        ride.setCrowding(crowding);
        ride.setNotes(notes);
        rideManager.addRide(ride);
        return Integer.toString(ride.getId());
    }

    @PutMapping("/api/updateRideLocation/{rideId}/{userName}")
    public String updateRideLocation(@RequestBody String location, @PathVariable String rideId, @PathVariable String userName) {
        JsonObject object = (JsonObject) JsonParser.parseString(location);
        double latitude = object.get("latitude").getAsDouble();
        double longitude = object.get("longitude").getAsDouble();
        Location newLocation = new Location(latitude, longitude);
        Ride ride = rideManager.findById(Integer.parseInt(rideId));
        rideManager.updateLocation(ride, newLocation);
        return Integer.toString(ride.getId());
    }

    @GetMapping("/api/getRidesByLine/{companyName}/{lineIdentifier}")
    public String getRidesByLine(@PathVariable String companyName, @PathVariable String lineIdentifier) {
        Line line = lineRepository.findById(new LineCompositeKey(companyName, lineIdentifier)).get();
        List<Ride> rides = rideManager.findByLine(line);

        JsonArray outputRides = new JsonArray();

        for (Ride ride: rides) {
            JsonObject rideJson = new JsonObject();
            Location location = ride.getLastLocation();
            rideJson.addProperty("id", ride.getId());
            rideJson.addProperty("lastLatitude", location.getLatitude());
            rideJson.addProperty("lastLongitude", location.getLongitude());
            rideJson.addProperty("companyName", companyName);
            rideJson.addProperty("lineIdentifier", lineIdentifier);
            rideJson.addProperty("crowding", ride.getCrowding());
            rideJson.addProperty("destination", ride.getDestination());
            JsonArray notes = new JsonArray();
            ride.getNotes().forEach(notes::add);
            rideJson.add("notes", notes);
            outputRides.add(rideJson);
        }

        return outputRides.toString();
    }

    @PostMapping("/api/terminateRide/{rideId}")
    public String terminateRide(@PathVariable String rideId) {
        Ride ride = rideManager.findById(Integer.parseInt(rideId));
        rideManager.removeRide(ride);

        return "OKAY";
    }
}