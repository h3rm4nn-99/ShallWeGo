package com.shallwego.server.logic.service;

import com.shallwego.server.logic.entities.Company;

import java.io.Serializable;
import java.util.Objects;


public class LineCompositeKey implements Serializable {
    private String identifier;
    private Company company;
    private String destination;

    public LineCompositeKey() {}

    public LineCompositeKey(String identifier, Company company, String destination) {
        this.identifier = identifier;
        this.company = company;
        this.destination = destination;
    }

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

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineCompositeKey that = (LineCompositeKey) o;
        return identifier.equals(that.identifier) && company.equals(that.company);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, company);
    }
}
