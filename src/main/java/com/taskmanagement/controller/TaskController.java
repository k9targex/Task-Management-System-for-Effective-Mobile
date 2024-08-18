package com.taskmanagement.controller;

import com.taskmanagement.model.entity.Task;
import com.taskmanagement.service.TaskService;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "Task Management", description = "Operations related to tasks")

@RestController
@RequestMapping("/tasks")
public class TaskController {
  private final TaskService taskService;

  @Autowired
  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }
  @Operation(summary = "Get all tasks", description = "Retrieve a list of all tasks.")
  @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of tasks")
  @GetMapping("")
  public ResponseEntity<List<Task>> getTasks() {
    return ResponseEntity.ok(taskService.getTasks());
  }
}
