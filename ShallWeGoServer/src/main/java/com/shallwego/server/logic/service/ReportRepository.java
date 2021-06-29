package com.shallwego.server.logic.service;

import com.shallwego.server.logic.entities.Report;
import com.shallwego.server.logic.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    List<Report> findByUser(User user);
}
