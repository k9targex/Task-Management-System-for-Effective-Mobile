package com.taskmanagement.dao;

import com.taskmanagement.model.entity.Task;
import com.taskmanagement.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"comments"})
    @Query("SELECT t FROM Task t WHERE t.author = :user OR t.performer = :user")
    List<Task> findAllTasksByUser(@Param("user") User user);
    @EntityGraph(attributePaths = {"comments"})
    Optional<Task> findTaskByIdAndAuthor(Long taskId,User user);

//    @EntityGraph(attributePaths = {"comments"})
//    List<Task> ();
    Optional<Task> findTaskByTitle(String title);
    @EntityGraph(attributePaths = {"comments"})
    Optional<Task> findByTitleAndAuthor(String title, User author);

}
