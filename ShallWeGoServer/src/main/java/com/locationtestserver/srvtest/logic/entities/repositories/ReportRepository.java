package com.locationtestserver.srvtest.logic.entities.repositories;

import com.locationtestserver.srvtest.logic.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    @Query("SELECT r FROM Report r WHERE r.class = LineReport AND r.company = :company")
    List<LineReport> findLineReportsByCompany(@Param("company") Company company);
    List<Report> findByUser(User user);
}
