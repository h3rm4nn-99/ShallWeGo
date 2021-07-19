package com.shallwego.server.logic.entities;

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
    private List<Line> linesAffectedEvent;

    private Date validityStart;
    private Date validityEnd;
    private String eventType;
    private String description;
    private String latitude;
    private String longitude;

    public List<Line> getLinesAffectedEvent() {
        return linesAffectedEvent;
    }

    public void setLinesAffectedEvent(List<Line> linesAffectedEvent) {
        this.linesAffectedEvent = linesAffectedEvent;
    }

    public Date getValidityStart() {
        return validityStart;
    }

    public void setValidityStart(Date validityStart) {
        this.validityStart = validityStart;
    }

    public Date getValidityEnd() {
        return validityEnd;
    }

    public void setValidityEnd(Date validityEnd) {
        this.validityEnd = validityEnd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
