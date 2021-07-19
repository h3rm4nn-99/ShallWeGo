package com.shallwego.server.logic.service;

import com.shallwego.server.logic.entities.TemporaryEventReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemporaryEventReportRepository extends JpaRepository<TemporaryEventReport, Integer> {}
