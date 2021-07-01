package com.shallwego.server.logic.entities;

import com.shallwego.server.logic.service.LineCompositeKey;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@IdClass(LineCompositeKey.class)
public class Line implements Serializable {

    public Line() {}

    @Id
    private String identifier;

    @ManyToOne
    @Id
    private Company company;

    private String firstTerminus;
    private String secondTerminus;

    @ManyToMany(mappedBy = "lines")
    private List<Stop> stops;

    @OneToOne(mappedBy = "lineAffected")
    private LineReport lineReport;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getFirstTerminus() {
        return firstTerminus;
    }

    public void setFirstTerminus(String firstTerminus) {
        this.firstTerminus = firstTerminus;
    }

    public String getSecondTerminus() {
        return secondTerminus;
    }

    public void setSecondTerminus(String secondTerminus) {
        this.secondTerminus = secondTerminus;
    }

    public Report getLineReport() {
        return lineReport;
    }

    public void setLineReport(LineReport lineReport) {
        this.lineReport = lineReport;
    }
}
