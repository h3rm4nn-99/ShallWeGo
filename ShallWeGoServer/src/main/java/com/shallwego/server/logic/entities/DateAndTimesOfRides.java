package com.shallwego.server.logic.entities;

import com.shallwego.server.logic.service.DateAndTimeOfRidesCompositeKey;

import javax.persistence.*;
import java.util.Map;

@Entity
@IdClass(DateAndTimeOfRidesCompositeKey.class)
public class DateAndTimesOfRides {
    @Id
    @ManyToOne
    private Line targetLine;

    private String destination;

    @Id
    @ManyToOne
    private Stop targetStop;

    @ElementCollection
    @JoinTable(name = "past_ride_times",
            inverseJoinColumns=@JoinColumn(name="list_of_rides_class_id"))
    @MapKeyColumn(name = "targetDay", length = 10)
    private Map<String, RidesServiceClass> pastRideTimes;
}
