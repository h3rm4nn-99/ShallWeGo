package com.locationtestserver.srvtest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

@RestController
public class Controller {
    @PutMapping("/api/putLocation")
    public String printLocation(@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude) {
        System.out.println("Latitudine " + latitude + "\nLongitudine " + longitude);
        return "OKAY\nLatitudine " + latitude + "\nLongitudine " + longitude;
    }
    @GetMapping("/api/createPeople")
    public String createPeople() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONArray array = (JSONArray) parser.parse(new FileReader("comuni.json"));
        Iterator<JSONObject> iterator = array.iterator();
        JSONArray resultArray = new JSONArray();

        while (iterator.hasNext()) {
            Random r = new Random();
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
                UtenteEntity entity = new UtenteEntity(username, comune, karma, permanenza);
                JSONObject target = new JSONObject();
                target.put("username", entity.getUserName());
                target.put("comune", entity.getComune());
                target.put("karma", entity.getKarma());
                target.put("permanenza", entity.getPermanenzaSullaPiattaforma());
                resultArray.add(target);
            }
        }
        return resultArray.toJSONString();
    }
}