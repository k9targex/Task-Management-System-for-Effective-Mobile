package com.taskmanagement.controller;

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
  public ResponseEntity<List<User>> getAllUsers() {
    return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
  }
  @PostMapping("/task")
  public ResponseEntity<String> createTask(@Valid @RequestBody TaskRequest taskRequest, HttpServletRequest request){
    userService.createTask(request,taskRequest);
    return ResponseEntity.ok("Task was successfully added");
  }
  @DeleteMapping("/task/{title}")
  public ResponseEntity<String> deleteTask(@PathVariable String title, HttpServletRequest request){
    userService.deleteTask(request,title);
    return ResponseEntity.ok("Task was successfully deleted");
  }

  @GetMapping("/task")
  public ResponseEntity<List<Task>> getTasks(HttpServletRequest request){
    return ResponseEntity.ok(userService.getTasks(request));
  }




}
