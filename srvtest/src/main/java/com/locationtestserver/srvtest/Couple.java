package com.locationtestserver.srvtest;

import java.util.Objects;

public class Couple {
    private Individual parent1, parent2;

    public Couple(Individual parent1, Individual parent2) {
        this.parent1 = parent1;
        this.parent2 = parent2;
    }

    public Individual getParent1() {
        return parent1;
    }

    public void setParent1(Individual parent1) {
        this.parent1 = parent1;
    }

    public Individual getParent2() {
        return parent2;
    }

    public void setParent2(Individual parent2) {
        this.parent2 = parent2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Couple)) return false;
        Couple couple = (Couple) o;
        return (parent1.equals(couple.parent1) && parent2.equals(couple.parent2)) || (parent1.equals(couple.parent2) && parent2.equals(couple.parent1));
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent1, parent2);
    }
}
