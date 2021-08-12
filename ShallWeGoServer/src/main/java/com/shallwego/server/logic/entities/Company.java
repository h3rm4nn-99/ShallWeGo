package com.shallwego.server.logic.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Company implements Serializable {

    public Company() {}

    @Id
    private String name;
    private String website;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "company")
    private List<Line> linee = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "company")
    private CompanyReport companyReport;

    public String getName() {
        return name;
    }

    public void addLine(Line l) {
        linee.add(l);
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

    public void setCompanyReport(CompanyReport companyReport) {
        this.companyReport = companyReport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company)) return false;
        Company company = (Company) o;
        return Objects.equals(getName(), company.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
