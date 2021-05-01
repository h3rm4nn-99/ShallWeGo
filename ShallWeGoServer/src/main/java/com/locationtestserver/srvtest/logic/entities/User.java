package com.locationtestserver.srvtest.logic.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
public class User implements Serializable {
    @Id
    private String userName;
    private String password;
    private String comune;
    private Double karma;
    private Integer permanenzaSullaPiattaforma;

    @OneToMany(mappedBy = "user")
    private List<Report> reports;

    public User() {}

    public User(String userName, String password, String comune, Double karma, Integer permanenzaSullaPiattaforma) {
        this.userName = userName;
        this.password = password;
        this.comune = comune;
        this.karma = karma;
        this.permanenzaSullaPiattaforma = permanenzaSullaPiattaforma;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getComune() {
        return comune;
    }

    public void setComune(String comune) {
        this.comune = comune;
    }

    public Double getKarma() {
        return karma;
    }

    public void setKarma(Double karma) {
        this.karma = karma;
    }

    public Integer getPermanenzaSullaPiattaforma() {
        return permanenzaSullaPiattaforma;
    }

    public void setPermanenzaSullaPiattaforma(Integer permanenzaSullaPiattaforma) {
        this.permanenzaSullaPiattaforma = permanenzaSullaPiattaforma;
    }

    @Override
    public String toString() {
        return "UtenteEntity{" +
                "userName='" + userName + '\'' +
                ", comune='" + comune + '\'' +
                ", karma=" + karma +
                ", permanenzaSullaPiattaforma=" + permanenzaSullaPiattaforma +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User that = (User) o;
        return userName.equals(that.userName) && comune.equals(that.comune) && karma.equals(that.karma) && permanenzaSullaPiattaforma.equals(that.permanenzaSullaPiattaforma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, comune, karma, permanenzaSullaPiattaforma);
    }
}
