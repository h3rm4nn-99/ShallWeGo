package com.shallwego.server.logic.service;

import java.io.Serializable;
import java.util.Objects;

public class LineCompositeKey implements Serializable {
    private String identifier;
    private String company;

    public LineCompositeKey() {}

    public LineCompositeKey(String identifier, String companyName) {
        this.identifier = identifier;
        this.company = companyName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
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
