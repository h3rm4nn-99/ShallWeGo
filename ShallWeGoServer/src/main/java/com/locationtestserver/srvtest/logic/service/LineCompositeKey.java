package com.locationtestserver.srvtest.logic.service;

import com.locationtestserver.srvtest.logic.entities.Company;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;


public class LineCompositeKey implements Serializable {
    private String identifier;
    private Company company;

    public LineCompositeKey() {}

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
