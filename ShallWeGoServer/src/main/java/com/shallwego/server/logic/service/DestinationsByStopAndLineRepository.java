package com.shallwego.server.logic.service;

import com.shallwego.server.logic.entities.DestinationsByStopAndLine;
import com.shallwego.server.logic.entities.Line;
import com.shallwego.server.logic.entities.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DestinationsByStopAndLineRepository extends JpaRepository<DestinationsByStopAndLine, Integer> {
    public List<DestinationsByStopAndLine> findByTargetLineAndTargetStop(Stop stop, Line line);
}