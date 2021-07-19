package com.shallwego.server.service;

import com.shallwego.server.logic.entities.Stop;
import com.shallwego.server.logic.service.StopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
@Order(1)
public class StopsCrowdingInitializer implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private StopRepository stopRepository;
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("Inizializzazione dei dati sull'affollamento delle fermate...");
        List<Stop> stops = stopRepository.findAll();
        HashMap<Integer, Integer> stopCrowding = new HashMap<>();
        for (Stop stop: stops) {
            stopCrowding.put(stop.getId(), 0);
        }
        Utils.setStopCrowding(stopCrowding);
        System.out.println("Inizializzazione Completata!");
    }
}
