package com.taskmanagement.dao;

import com.taskmanagement.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findTaskByTitle(String title);
}
