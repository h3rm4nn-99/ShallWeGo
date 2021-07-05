package com.shallwego.server.logic.entities;

import javax.persistence.*;
import java.util.List;

@Entity
public class RidesServiceClass {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ElementCollection
    private List<String> rides;
}
