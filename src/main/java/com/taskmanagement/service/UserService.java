package com.taskmanagement.service;

import com.taskmanagement.dao.TaskRepository;
import com.taskmanagement.dao.UserRepository;
import com.taskmanagement.exception.PerformerNotFound;
import com.taskmanagement.exception.TaskAlreadyExistException;
import com.taskmanagement.exception.TaskNotFoundException;
import com.taskmanagement.model.Comment;
import com.taskmanagement.model.RoleList;
import com.taskmanagement.model.dto.TaskRequest;
import com.taskmanagement.model.entity.Task;
import com.taskmanagement.model.entity.User;
import com.taskmanagement.security.JwtCore;
import com.taskmanagement.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@CacheConfig(cacheNames = "data")
public class UserService implements UserDetailsService {
  private static final String USER_NOT_FOUND_MESSAGE = "User with name \"%s\" does not exist";
  private static final String USER_ID_NOT_FOUND_MESSAGE = "User with id \"%s\" does not exist";

  private static final String PERFORMER_ID_NOT_FOUND_MESSAGE =
      "Performer with ID \"%s\" does not exist";

  private static final String TASK_NOT_FOUND_MESSAGE = "Task with name \"%s\" does not exist";
  private static final String TASK_ID_NOT_FOUND_MESSAGE =
      "Task with ID \"%s\" does not exist for this author";
  private static final String TASK_ALREADY_EXIST = "Task with name \"%s\" already exist";

  private final UserRepository userRepository;
  private final TaskRepository taskRepository;

  private JwtCore jwtCore;

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

  public void createTask(HttpServletRequest request, TaskRequest taskRequest) {
    User user = getUserFromRequest(request);
    if (taskRepository.findByTitleAndAuthor(taskRequest.getTitle(), user).isPresent()) {
      throw new TaskAlreadyExistException(
          String.format(TASK_ALREADY_EXIST, taskRequest.getTitle()));
    }
    Task task =
        Task.builder()
            .title(taskRequest.getTitle())
            .description(taskRequest.getDescription())
            .status(taskRequest.getStatus())
            .priority(taskRequest.getPriority())
            .author(user)
            .build();

    user.getTasks().add(task);
    userRepository.save(user);
  }

  public void deleteTask(HttpServletRequest request, Long taskId) {
    User author = getUserFromRequest(request);

    Task task =
        taskRepository
            .findTaskByIdAndAuthor(taskId, author)
            .orElseThrow(
                () -> new TaskNotFoundException(String.format(TASK_ID_NOT_FOUND_MESSAGE, taskId)));

    author.getTasks().remove(task);


    userRepository.save(author);
    taskRepository.delete(task);
  }

    public List<Task> getUserTasks(HttpServletRequest request) {
      return taskRepository.findAllTasksByUser( getUserFromRequest(request));
    }
  public List<Task> getUserTasksById(Long userID) {
    return taskRepository.findAllTasksByUser(userRepository.findUserById(userID) .orElseThrow(
            () ->
                    new UsernameNotFoundException(
                            String.format(USER_ID_NOT_FOUND_MESSAGE,userID))));
  }
  public void addPerformer(Long taskId, Long performerId) {
    Task task =
        taskRepository
            .findById(taskId)
            .orElseThrow(
                () -> new TaskNotFoundException(String.format(TASK_ID_NOT_FOUND_MESSAGE, taskId)));

    User performer =
        userRepository
            .findByIdAndRole(performerId, RoleList.PERFORMER)
            .orElseThrow(
                () ->
                    new PerformerNotFound(
                        String.format(PERFORMER_ID_NOT_FOUND_MESSAGE, performerId)));

    Optional.ofNullable(task.getPerformer())
        .ifPresent(
            perform -> {
              perform.getTasks().remove(task);
              userRepository.save(perform);
            });
    task.setPerformer(performer);
    performer.getTasks().add(task);
    userRepository.save(performer);
    taskRepository.save(task);
  }


  public void addCommentToTask(Long taskId, String commentText,HttpServletRequest request) {
    Task task = taskRepository.findTaskByIdAndAuthor(taskId,getUserFromRequest(request))
            .orElseThrow(() -> new TaskNotFoundException(String.format(TASK_ID_NOT_FOUND_MESSAGE,taskId)));
    String username = getUserFromRequest(request).getUsername();
    Comment comment = Comment.builder()
            .text(commentText)
            .author(username)
            .timestamp(LocalDateTime.now())
            .build();
    task.getComments().add(comment);
    taskRepository.save(task);
  }

  private User getUserFromRequest(HttpServletRequest request) {
    String token = jwtCore.getTokenFromRequest(request);
    return userRepository
        .findUserByUsername(jwtCore.getNameFromJwt(token))
        .orElseThrow(
            () ->
                new UsernameNotFoundException(
                    String.format(USER_NOT_FOUND_MESSAGE, jwtCore.getNameFromJwt(token))));
  }
}
