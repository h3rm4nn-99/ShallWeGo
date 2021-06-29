package com.shallwego.server.logic.entities;


import org.apache.commons.codec.digest.DigestUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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
    private String provincia;
    private Double karma;
    private Integer permanenzaSullaPiattaforma;

    @OneToMany(mappedBy = "user")
    private List<Report> reports;

    @ManyToMany
    private List<Report> assignedTo;

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
        User that = (User) o;
        return userName.equals(that.userName) && comune.equals(that.comune) && karma.equals(that.karma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, comune, karma);
    }
}
