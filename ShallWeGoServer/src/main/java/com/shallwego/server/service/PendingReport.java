package com.shallwego.server.service;

import com.shallwego.server.logic.entities.Report;
import com.shallwego.server.logic.entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PendingReport {
    private int id;
    private Report report;

    private final HashMap<User, Integer> verifiersPool = new HashMap<>();

    public PendingReport(int id, Report report, Set<User> verifiers) {
        this.id = id;
        this.report = report;
        for (User verifier: verifiers) {
            verifiersPool.put(verifier, 0);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public void addVote(User user, int vote) {
        verifiersPool.put(user, vote);
    }

    public HashMap<User, Integer> getVerifiersPool() {
        return verifiersPool;
    }

    public int checkStatus() {
        int size = verifiersPool.keySet().size();
        int okay = 0;
        int nok = 0;

        for (User user: verifiersPool.keySet()) {
            Integer current = verifiersPool.get(user);
            if (current == 1) {
                okay++;
            } else if (current == -1) {
                nok++;
            }
        }

        if (okay >= size / 2) return ReportStatus.REPORT_ACCEPTED;
        if (nok >= size / 2) return ReportStatus.REPORT_REJECTED;
        return ReportStatus.REPORT_PENDING;
    }

    public int getSumOfVotes() {
        int sum = 0;

        for (User user: verifiersPool.keySet()) {
            sum += verifiersPool.get(user);
        }
        return sum;
    }

    public int getUserVote(User user) {
        return verifiersPool.get(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PendingReport)) return false;
        PendingReport that = (PendingReport) o;
        return getId() == that.getId() && Objects.equals(getReport(), that.getReport());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getReport());
    }

    @Override
    public String toString() {
        return "PendingReport{" +
                "id=" + id +
                ", report=" + report +
                '}';
    }

    static class ReportStatus {
        public static final int REPORT_PENDING = 0;
        public static final int REPORT_ACCEPTED = 1;
        public static final int REPORT_REJECTED = -1;
    }
}
