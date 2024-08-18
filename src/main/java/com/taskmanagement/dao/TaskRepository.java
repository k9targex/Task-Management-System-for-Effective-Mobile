package com.taskmanagement.dao;

import com.taskmanagement.model.Comment;
import com.taskmanagement.model.TaskPriority;
import com.taskmanagement.model.TaskStatus;
import com.taskmanagement.model.entity.Task;
import com.taskmanagement.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

  @EntityGraph(attributePaths = {"comments", "author", "performer"})
  @Query("SELECT t FROM Task t WHERE t.author = :user OR t.performer = :user")
  List<Task> findAllTasksByUser(@Param("user") User user);

  @EntityGraph(attributePaths = {"comments"})
  Optional<Task> findTaskByIdAndAuthor(Long taskId, User user);

  @EntityGraph(attributePaths = {"comments"})
  @Query("SELECT t FROM Task t WHERE t.id = :taskId AND (t.author = :user OR t.performer = :user)")
  Optional<Task> findTaskByIdAndUser(@Param("taskId") Long taskId, @Param("user") User user);

  Optional<Task> findTaskByIdAndPerformer(Long taskId, User user);

  Optional<Task> findById(Long taskId);

  boolean existsByTitleAndAuthor(String title, User author);

  @Query("SELECT t.comments FROM Task t WHERE t.id = :taskId")
  List<Comment> findCommentsByTaskId(@Param("taskId") Long taskId);



  @EntityGraph(attributePaths = {"comments"})
  @Query("SELECT t FROM Task t WHERE (t.author = :user OR t.performer = :user)")
  Page<Task> findAllTasksByUser(@Param("user") User user, Pageable pageable);

  @EntityGraph(attributePaths = {"comments"})
  @Query("SELECT t FROM Task t WHERE (t.author = :user OR t.performer = :user) AND t.status = :status")
  Page<Task> findAllTasksByUserAndStatus(@Param("user") User user, @Param("status") TaskStatus status, Pageable pageable);

  @EntityGraph(attributePaths = {"comments"})
  @Query("SELECT t FROM Task t WHERE (t.author = :user OR t.performer = :user) AND t.priority = :priority")
  Page<Task> findAllTasksByUserAndPriority(@Param("user") User user, @Param("priority") TaskPriority priority, Pageable pageable);

  @EntityGraph(attributePaths = {"comments"})
  @Query("SELECT t FROM Task t WHERE (t.author = :user OR t.performer = :user) AND t.status = :status AND t.priority = :priority")
  Page<Task> findAllTasksByUserAndStatusAndPriority(@Param("user") User user, @Param("status") TaskStatus status, @Param("priority") TaskPriority priority, Pageable pageable);

}
