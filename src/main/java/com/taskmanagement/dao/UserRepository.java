package com.taskmanagement.dao;

import com.taskmanagement.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUsername(String username);
    Optional<User> findUsersByEmail(String email);
    Boolean existsUserByUsername(String username);
    Boolean existsUserByEmail(String email);


}

