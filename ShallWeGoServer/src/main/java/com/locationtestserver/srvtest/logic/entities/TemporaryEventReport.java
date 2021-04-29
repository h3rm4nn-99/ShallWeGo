package com.locationtestserver.srvtest.logic.entities;

import com.locationtestserver.srvtest.service.Location;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
public class TemporaryEventReport extends Report implements Serializable {

    public TemporaryEventReport() {
        super();
    }

    @ManyToMany
    private List<Line> linesAffected;

    private Date validityStart;
    private Date validityEnd;
    private String description;
    private String latitude;
    private String longitude;
}
