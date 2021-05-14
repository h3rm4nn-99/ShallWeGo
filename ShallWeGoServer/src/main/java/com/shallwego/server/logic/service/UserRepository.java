package com.shallwego.server.logic.service;

import com.shallwego.server.logic.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    List<User> findByProvincia(String provincia);
}
