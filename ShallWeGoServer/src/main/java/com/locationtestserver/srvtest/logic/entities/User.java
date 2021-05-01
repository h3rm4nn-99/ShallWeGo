package com.locationtestserver.srvtest.logic.entities;

import com.locationtestserver.srvtest.service.Location;
import com.locationtestserver.srvtest.service.Utils;
import org.json.simple.parser.ParseException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
public class User implements Serializable {
    @Id
    private String userName;
    private String password;
    private String comune;
    private String provincia;
    private Double karma;
    private Integer permanenzaSullaPiattaforma;

    @OneToMany(mappedBy = "user")
    private List<Report> reports;

    public User() {}

    public User(String userName, String password, String comune, String provincia, Double karma, Integer permanenzaSullaPiattaforma) {
        this.userName = userName;
        this.password = password;
        this.comune = comune;
        this.provincia = provincia;
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

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public double getFitness(Location location) throws IOException, ParseException {
        double distance = location.distance(this.getComune(), this.getProvincia());
        double distancePartialFitness;
        if (distance == 0) {
            distancePartialFitness = 15 / (distance + 0.1); //avoid division by 0
        } else {
            distancePartialFitness = 15 / distance;
        }

        double karmaPartialFitness;

        if (this.getKarma() < 42) {
            karmaPartialFitness = Math.pow(this.getKarma(), 0.6);
        } else {
            karmaPartialFitness = Math.pow(1.1, this.getKarma());
        }

        return ((3 * distancePartialFitness) + (2 * karmaPartialFitness)) / 2;
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
