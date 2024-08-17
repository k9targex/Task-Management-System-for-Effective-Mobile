package com.taskmanagement.controller;

import com.taskmanagement.model.dto.CommentRequest;
import com.taskmanagement.model.dto.TaskRequest;
import com.taskmanagement.model.entity.Task;
import com.taskmanagement.model.entity.User;
import com.taskmanagement.security.JwtCore;
import com.taskmanagement.service.UserService;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  private JwtCore jwtCore;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Autowired
  public void setJwtCore(JwtCore jwtCore) {
    this.jwtCore = jwtCore;
  }

  @GetMapping()
  public ResponseEntity<List<User>> getAllAuthors() {
    return new ResponseEntity<>(userService.getAllAuthors(), HttpStatus.OK);
  }
  @PostMapping("/tasks")
  public ResponseEntity<String> createTask(@Valid @RequestBody TaskRequest taskRequest, HttpServletRequest request){
      userService.createTask(request,taskRequest);
      return ResponseEntity.ok("Task was successfully added");
  }
  @DeleteMapping("/tasks/{task_id}")
  public ResponseEntity<String> deleteTask(@PathVariable Long task_id, HttpServletRequest request){
    userService.deleteTask(request,task_id);
    return ResponseEntity.ok("Task was successfully deleted");
  }

  @GetMapping("/tasks")
  public ResponseEntity<List<Task>> getUserTasks(HttpServletRequest request){
    return ResponseEntity.ok(userService.getUserTasks(request));
  }

  @GetMapping("/tasks/{user_id}")
  public ResponseEntity<List<Task>> getTasksByUserId(@PathVariable Long user_id){
    return ResponseEntity.ok(userService.getUserTasksById(user_id));
  }

  @PatchMapping("/tasks/{task_id}/{performer_id}")
  public ResponseEntity<String> addPerformer(@PathVariable Long task_id,@PathVariable Long performer_id){
    userService.addPerformer(task_id,performer_id);
    return ResponseEntity.ok("Performer was successfully added");
  }
  @PostMapping("/tasks/{task_id}")
  public ResponseEntity<String> addComment(@PathVariable Long task_id, @RequestBody CommentRequest commentRequest, HttpServletRequest request){
    userService.addCommentToTask(task_id,commentRequest.getComment(),request);
    return ResponseEntity.ok("Comment was successfully added");
  }



}
