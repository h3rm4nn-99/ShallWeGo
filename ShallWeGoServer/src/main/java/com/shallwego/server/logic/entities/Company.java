package com.shallwego.server.logic.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
public class Company implements Serializable {

    public Company() {}

    @Id
    private String name;
    private String website;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "company")
    private List<Line> linee;

    @OneToOne(cascade = CascadeType.ALL)
    private Report companyReport;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<Line> getLinee() {
        return linee;
    }

    public void setLinee(List<Line> linee) {
        this.linee = linee;
    }

    public Report getCompanyReport() {
        return companyReport;
    }

    public void setCompanyReport(Report companyReport) {
        this.companyReport = companyReport;
    }
}
