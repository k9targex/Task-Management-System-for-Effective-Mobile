package com.taskmanagement.dao;

import com.taskmanagement.model.entity.Task;
import com.taskmanagement.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findTaskByTitle(String title);
    Optional<List<Task>> findTasksByUsers(User user);
    Optional<Task> findByTitleAndUsers(String title, User user);

}
