package com.shallwego.server.logic.entities;


import org.apache.commons.codec.digest.DigestUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
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

    @ManyToMany
    private List<Report> assignedTo = new ArrayList<>();

    @ManyToMany
    private List<Stop> preferredStops = new ArrayList<>();

    public User() {}

    public User(String userName, String password, String comune, String provincia, Double karma) {
        this.userName = userName;
        this.password = DigestUtils.sha512Hex(password);
        this.comune = comune;
        this.provincia = provincia;
        this.karma = karma;
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
        this.password = DigestUtils.sha512Hex(password);
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

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public Integer getPermanenzaSullaPiattaforma() {
        return permanenzaSullaPiattaforma;
    }

    public void setPermanenzaSullaPiattaforma(Integer permanenzaSullaPiattaforma) {
        this.permanenzaSullaPiattaforma = permanenzaSullaPiattaforma;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public List<Report> getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(List<Report> assignedTo) {
        this.assignedTo = assignedTo;
    }

    public List<Stop> getPreferredStops() {
        return preferredStops;
    }

    public void setPreferredStops(List<Stop> preferredStops) {
        this.preferredStops = preferredStops;
    }

    public void addPreferredStop(Stop s) {
        this.preferredStops.add(s);
    }

    public void removePreferredStop(Stop s) {
        this.preferredStops.remove(s);
    }

    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", comune='" + comune + '\'' +
                ", provincia='" + provincia + '\'' +
                ", karma=" + karma +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getUserName(), user.getUserName()) && Objects.equals(getPassword(), user.getPassword()) && Objects.equals(getComune(), user.getComune()) && Objects.equals(getProvincia(), user.getProvincia()) && Objects.equals(getKarma(), user.getKarma()) && Objects.equals(getPermanenzaSullaPiattaforma(), user.getPermanenzaSullaPiattaforma()) && Objects.equals(getReports(), user.getReports()) && Objects.equals(getAssignedTo(), user.getAssignedTo()) && Objects.equals(getPreferredStops(), user.getPreferredStops());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserName(), getPassword(), getComune(), getProvincia(), getKarma(), getPermanenzaSullaPiattaforma(), getReports(), getAssignedTo(), getPreferredStops());
    }
}
