package com.shallwego.server.logic.service;

import com.shallwego.server.logic.entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, String> {
}
