package com.taskmanagement.dao;

import com.taskmanagement.model.RoleList;
import com.taskmanagement.model.entity.Task;
import com.taskmanagement.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndRole( Long userId, RoleList role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tasks WHERE u.role = :role")
    List<User> findAllWithTasksByRole(@Param("role") RoleList role);

    Optional<User> findUserById(Long userId);
    Optional<User> findUserByUsername(String username);
    Optional<User> findUsersByEmail(String email);
    Boolean existsUserByUsername(String username);
    Boolean existsUserByEmail(String email);


}

