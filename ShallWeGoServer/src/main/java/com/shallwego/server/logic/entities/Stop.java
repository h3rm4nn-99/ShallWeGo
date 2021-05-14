package com.shallwego.server.logic.entities;

import javax.persistence.*;
import java.util.List;

@Entity
public class Stop {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @OneToMany
    private List<Line> lines;

    private Boolean hasShelter;
    private Boolean hasTimeTables;
    private Boolean hasStopSign;

    private Double latitude;
    private Double longitude;
}
