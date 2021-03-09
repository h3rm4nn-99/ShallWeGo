package com.locationtestserver.srvtest;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @PutMapping("/api/putLocation")
    public String printLocation(@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude) {
        System.out.println("Latitudine " + latitude + "\nLongitudine " + longitude);
        return "OKAY\nLatitudine " + latitude + "\nLongitudine " + longitude;
    }
}
