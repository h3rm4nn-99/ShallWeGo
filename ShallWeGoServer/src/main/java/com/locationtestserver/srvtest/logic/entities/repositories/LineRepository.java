package com.locationtestserver.srvtest.logic.entities.repositories;

import com.locationtestserver.srvtest.logic.entities.Company;
import com.locationtestserver.srvtest.logic.entities.Line;
import com.locationtestserver.srvtest.logic.service.LineCompositeKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LineRepository extends JpaRepository<Line, LineCompositeKey> {
    List<Line> findByCompany(Company company);
}
