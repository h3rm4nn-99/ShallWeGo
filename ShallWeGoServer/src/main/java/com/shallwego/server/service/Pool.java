package com.shallwego.server.service;

import com.shallwego.server.logic.entities.*;
import com.shallwego.server.logic.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

@Component
@Scope("singleton")
@Transactional
public class Pool {
    private final HashSet<PendingReport> pendingReports = new HashSet<>();
    private int lastId = 0;

    @Autowired
    private UserRepository repository;

    @Autowired
    private DestinationsByStopAndLineRepository destinationsByStopAndLineRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private CompanyRepository companyRepository;


    public List<PendingReport> getPendingByUser(User user) {
        List<PendingReport> reports = new ArrayList<>();
        pendingReports.forEach((pendingReport) -> {
            if (pendingReport.getReport().getUser().equals(user)) {
                reports.add(pendingReport);
            }
        });
        return reports;
    }

    public PendingReport findById(Integer id) {
        for (PendingReport report: pendingReports) {
            if (report.getId() == id) {
                return report;
            }
        }

        return null;
    }

    public synchronized void addPendingReports(Report report, Set<User> verifiers) {
        pendingReports.add(new PendingReport(lastId++, report, verifiers));
        System.out.println(pendingReports.toString());
    }

    public synchronized void addPendingReports(Report report, Set<User> verifiers, HashMap<Line, List<String>> destinations) {
        pendingReports.add(new PendingStopReport(lastId++, report, verifiers, destinations));
        System.out.println(pendingReports.toString());
    }

    public int verifyReport(int id, User user, int vote) {
        PendingReport report = findById(id);
        report.addVote(user, vote);
        int reportStatus = report.checkStatus();
        if (reportStatus == PendingReport.ReportStatus.REPORT_REJECTED) {
            pendingReports.remove(report); //rejected
            return PendingReport.ReportStatus.REPORT_REJECTED;
        } else if (reportStatus == PendingReport.ReportStatus.REPORT_ACCEPTED) {
            for (User user1 : report.getVerifiersPool().keySet()) {
                user1.addAssignedTo(report.getReport());
                report.getReport().addVerifier(user1);
            }
            if (report.getReport() instanceof StopReport) {
                PendingStopReport pendingStopReport = (PendingStopReport) report;
                StopReport stopReport = (StopReport) report.getReport();
                Stop reportedStop = stopReport.getStopReported();
                for (Line line: reportedStop.getLines()) {
                    DestinationsByStopAndLine reachableFromThisStop = new DestinationsByStopAndLine();
                    reachableFromThisStop.setTargetStop(reportedStop);
                    reachableFromThisStop.setTargetLine(line);
                    reachableFromThisStop.setTargetDestinations(pendingStopReport.getDestinations().get(line));
                    line.addDestinationByLine(reachableFromThisStop);
                    reportedStop.addDestinationsByStop(reachableFromThisStop);
                    destinationsByStopAndLineRepository.save(reachableFromThisStop);
                }
                reportedStop.setStopReport(stopReport);
                stopRepository.saveAndFlush(reportedStop);
            } else if (report.getReport() instanceof LineReport) {
                LineReport lineReport = (LineReport) report.getReport();
                Line lineAffected = lineReport.getLineAffected();
                lineAffected.setLineReport(lineReport);
                lineRepository.saveAndFlush(lineAffected);

            } else if (report.getReport() instanceof CompanyReport) {
                Company company = ((CompanyReport) report.getReport()).getCompany();
                company.setCompanyReport((CompanyReport) report.getReport());
                companyRepository.saveAndFlush(company);

            }
            pendingReports.remove(report);
            return PendingReport.ReportStatus.REPORT_ACCEPTED;
        }
        return PendingReport.ReportStatus.REPORT_PENDING;
    }

    public List<PendingReport> reportsByAssignedUser(User user) {
        ArrayList<PendingReport> target = new ArrayList<>();
        for (PendingReport report: this.pendingReports) {
            if (report.getVerifiersPool().keySet().contains(user)) {
                target.add(report);
            }
        }

        return target;
    }
}
