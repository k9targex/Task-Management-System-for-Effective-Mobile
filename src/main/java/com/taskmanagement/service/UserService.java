package com.taskmanagement.service;

import com.taskmanagement.dao.TaskRepository;
import com.taskmanagement.dao.UserRepository;
import com.taskmanagement.exception.PerformerNotFound;
import com.taskmanagement.exception.TaskAlreadyExistException;
import com.taskmanagement.exception.TaskNotFoundException;
import com.taskmanagement.model.Comment;
import com.taskmanagement.model.RoleList;
import com.taskmanagement.model.TaskPriority;
import com.taskmanagement.model.TaskStatus;
import com.taskmanagement.model.dto.TaskRequest;
import com.taskmanagement.model.dto.TaskUpdateRequest;
import com.taskmanagement.model.dto.UpdateStatusRequest;
import com.taskmanagement.model.entity.Task;
import com.taskmanagement.model.entity.User;
import com.taskmanagement.security.JwtCore;
import com.taskmanagement.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@CacheConfig(cacheNames = "tasks")
public class UserService implements UserDetailsService {

  private static final String USER_NOT_FOUND_MESSAGE = "User with name \"%s\" does not exist";
  private static final String USER_ID_NOT_FOUND_MESSAGE = "User with id \"%s\" does not exist";
  private static final String PERFORMER_ID_NOT_FOUND_MESSAGE =
      "Performer with ID \"%s\" does not exist";
  private static final String TASK_NOT_FOUND_MESSAGE = "Task does not exist";
  private static final String TASK_ID_NOT_FOUND_MESSAGE =
      "Task with ID \"%s\" does not exist for this author";
  private static final String TASK_ID_NOT_FOUND_MESSAGE_FOR_PERFORMER =
      "Task with ID \"%s\" does not exist for this performer";
  private static final String TASK_ID_NOT_FOUND_MESSAGE_FOR_BOTH =
      "Task with ID \"%s\" does not exist for this user";
  private static final String TASK_ALREADY_EXIST = "Task with name \"%s\" already exist";

  private final UserRepository userRepository;
  private final TaskRepository taskRepository;
  private final JwtCore jwtCore;

  @Autowired
  public UserService(
      UserRepository userRepository, TaskRepository taskRepository, JwtCore jwtCore) {
    this.userRepository = userRepository;
    this.taskRepository = taskRepository;
    this.jwtCore = jwtCore;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findUserByUsername(username)
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(String.format(USER_NOT_FOUND_MESSAGE, username)));
    return UserDetailsImpl.build(user);
  }

  public List<User> getAllAuthors() {
    return userRepository.findAllWithTasksByRole(RoleList.AUTHOR);
  }

  @CachePut(key = "#taskRequest.title")
  public void createTask(HttpServletRequest request, TaskRequest taskRequest) {
    User user = getUserFromRequest(request);
    checkTaskExistence(taskRequest.getTitle(), user);
    Task task =
        Task.builder()
            .title(taskRequest.getTitle())
            .description(taskRequest.getDescription())
            .comments(new ArrayList<>())
            .status(taskRequest.getStatus())
            .priority(taskRequest.getPriority())
            .author(user)
            .build();
    taskRepository.save(task);
    user.getTasks().add(task);
    userRepository.save(user);

    Optional.ofNullable(taskRequest.getComment())
        .filter(comment -> !comment.isEmpty())
        .ifPresent(comment -> addCommentToTask(task.getId(), comment, request));
  }

  @CacheEvict(key = "#taskId")
  public void deleteTask(HttpServletRequest request, Long taskId) {
    Task task = findTaskByAuthor(request, taskId);
    task.getAuthor().getTasks().remove(task);
    userRepository.save(task.getAuthor());
    taskRepository.delete(task);
  }

  @Cacheable(key = "#request.userId")
  public List<Task> getUserTasks(HttpServletRequest request) {
    return taskRepository.findAllTasksByUser(getUserFromRequest(request));
  }

  @CachePut(key = "#taskId")
  public void addPerformer(Long taskId, Long performerId) {
    Task task = findTaskById(taskId);
    User performer = findPerformerById(performerId);

    Optional.ofNullable(task.getPerformer())
        .ifPresent(
            existingPerformer -> {
              existingPerformer.getTasks().remove(task);
              userRepository.save(existingPerformer);
            });

    task.setPerformer(performer);
    performer.getTasks().add(task);
    userRepository.save(performer);
    taskRepository.save(task);
  }

  @CachePut(key = "#taskId")
  public void addCommentToTask(Long taskId, String commentText, HttpServletRequest request) {
    Task task =
        taskRepository
            .findTaskByIdAndUser(taskId, getUserFromRequest(request))
            .orElseThrow(
                () ->
                    new TaskNotFoundException(
                        String.format(TASK_ID_NOT_FOUND_MESSAGE_FOR_BOTH, taskId)));
    String username = getUserFromRequest(request).getUsername();
    Comment comment =
        Comment.builder().text(commentText).author(username).timestamp(LocalDateTime.now()).build();
    task.getComments().add(comment);
    taskRepository.save(task);
  }

  @CachePut(key = "#taskId")
  public void updateTask(Long taskId, TaskUpdateRequest updateRequest, HttpServletRequest request) {
    Task task = findTaskByAuthor(request, taskId);
    checkTaskExistence(updateRequest.getTitle(), task.getAuthor());

    updateIfPresent(updateRequest.getTitle(), task::setTitle);
    updateIfPresent(updateRequest.getDescription(), task::setDescription);
    updateIfPresent(updateRequest.getStatus(), task::setStatus);
    updateIfPresent(updateRequest.getPriority(), task::setPriority);

    taskRepository.save(task);
  }

  @CachePut(key = "#taskId")
  public void updateStatus(
      UpdateStatusRequest updateStatusRequest, Long taskId, HttpServletRequest request) {
    Task task = findTaskByPerformer(request, taskId);
    task.setStatus(updateStatusRequest.getStatus());
    taskRepository.save(task);
  }

  @Cacheable(key = "#taskId")
  public List<Comment> getCommentsByTaskId(Long taskId) {
    findTaskById(taskId);
    return taskRepository.findCommentsByTaskId(taskId);
  }

  @Cacheable(key = "#userId")
  public Page<Task> getUserTasksByIdWithFilters(
      Long userId, Pageable pageable, TaskStatus status, TaskPriority priority) {
    User user =
        userRepository
            .findUserById(userId)
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        String.format(USER_ID_NOT_FOUND_MESSAGE, userId)));

    return Optional.ofNullable(status)
        .flatMap(
            s ->
                Optional.ofNullable(priority)
                    .map(
                        p ->
                            taskRepository.findAllTasksByUserAndStatusAndPriority(
                                user, s, p, pageable)))
        .orElseGet(
            () ->
                Optional.ofNullable(status)
                    .map(s -> taskRepository.findAllTasksByUserAndStatus(user, s, pageable))
                    .orElseGet(
                        () -> Optional.ofNullable(priority)
                                .map(p -> taskRepository.findAllTasksByUserAndPriority(user, p, pageable))
                                .orElseGet(
                                    () -> taskRepository.findAllTasksByUser(user, pageable))));}

  // Helper methods

  private User getUserFromRequest(HttpServletRequest request) {
    String token = jwtCore.getTokenFromRequest(request);
    return userRepository
        .findUserByUsername(jwtCore.getNameFromJwt(token))
        .orElseThrow(
            () ->
                new UsernameNotFoundException(
                    String.format(USER_NOT_FOUND_MESSAGE, jwtCore.getNameFromJwt(token))));}

  private void checkTaskExistence(String title, User user) {
    if (taskRepository.existsByTitleAndAuthor(title, user)) {
      throw new TaskAlreadyExistException(String.format(TASK_ALREADY_EXIST, title));
    }
  }

  private Task findTaskById(Long taskId) {
    return taskRepository
        .findById(taskId)
        .orElseThrow(
            () -> new TaskNotFoundException(String.format(TASK_NOT_FOUND_MESSAGE, taskId)));
  }

  private Task findTaskByAuthor(HttpServletRequest request, Long taskId) {
    User author = getUserFromRequest(request);
    return taskRepository
        .findTaskByIdAndAuthor(taskId, author)
        .orElseThrow(
            () -> new TaskNotFoundException(String.format(TASK_ID_NOT_FOUND_MESSAGE, taskId)));}

  private Task findTaskByPerformer(HttpServletRequest request, Long taskId) {
    User performer = getUserFromRequest(request);
    return taskRepository
        .findTaskByIdAndPerformer(taskId, performer)
        .orElseThrow(
            () ->
                new TaskNotFoundException(
                    String.format(TASK_ID_NOT_FOUND_MESSAGE_FOR_PERFORMER, taskId)));
  }

  private User findPerformerById(Long performerId) {
    return userRepository
        .findByIdAndRole(performerId, RoleList.PERFORMER)
        .orElseThrow(
            () ->
                new PerformerNotFound(String.format(PERFORMER_ID_NOT_FOUND_MESSAGE, performerId)));
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);}

}
