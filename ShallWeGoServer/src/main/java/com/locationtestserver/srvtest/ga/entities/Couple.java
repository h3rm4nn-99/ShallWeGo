package com.locationtestserver.srvtest.ga.entities;

import java.util.Objects;

public class Couple {
    private Individual individual1, individual2;

    public Couple(Individual individual1, Individual individual2) {
        this.individual1 = individual1;
        this.individual2 = individual2;
    }

    public Individual getindividual1() {
        return individual1;
    }

    public void setindividual1(Individual individual1) {
        this.individual1 = individual1;
    }

    public Individual getindividual2() {
        return individual2;
    }

    public void setindividual2(Individual individual2) {
        this.individual2 = individual2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Couple)) return false;
        Couple couple = (Couple) o;
        return (individual1.equals(couple.individual1) && individual2.equals(couple.individual2)) || (individual1.equals(couple.individual2) && individual2.equals(couple.individual1));
    }

    @Override
    public int hashCode() {
        return Objects.hash(individual1, individual2);
    }
}
