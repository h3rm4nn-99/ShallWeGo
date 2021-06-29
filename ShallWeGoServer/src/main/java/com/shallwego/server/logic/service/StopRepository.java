package com.shallwego.server.logic.service;

import com.shallwego.server.logic.entities.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StopRepository extends JpaRepository<Stop, Integer> {
}
