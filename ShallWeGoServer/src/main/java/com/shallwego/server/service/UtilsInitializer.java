package com.shallwego.server.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Component
@Order(0)
public class UtilsInitializer implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("Inizializzazione del database dei comuni...");
        HashMap<String, List<String>> province = new HashMap<>();
        JSONParser parser = new JSONParser();
        Random r = new Random();
        JSONArray array = null;
        try {
            array = (JSONArray) parser.parse(new FileReader("comuni.json"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Iterator<JSONObject> iterator = array.iterator();

        while (iterator.hasNext()) {
            JSONObject obj = iterator.next();
            JSONObject provinciaCurrent = (JSONObject) obj.get("provincia");
            String provincia = (String) provinciaCurrent.get("nome");
            String comune = (String) obj.get("nome");
            System.out.println(comune);
            if (!province.containsKey(provincia)) {
                province.put(provincia, new ArrayList<>());
            }
            province.get(provincia).add(comune);
        }

        Utils.setProvince(province);
        System.out.println("Completato");
    }
}
