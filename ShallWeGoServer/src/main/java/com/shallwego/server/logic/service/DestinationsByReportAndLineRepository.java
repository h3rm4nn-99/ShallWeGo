package com.shallwego.server.logic.service;

import com.shallwego.server.logic.entities.DestinationsByReportAndLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinationsByReportAndLineRepository extends JpaRepository<DestinationsByReportAndLine, Integer> {
}