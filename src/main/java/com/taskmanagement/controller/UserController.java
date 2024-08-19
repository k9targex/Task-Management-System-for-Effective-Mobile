package com.taskmanagement.controller;

import com.taskmanagement.model.Comment;
import com.taskmanagement.model.TaskPriority;
import com.taskmanagement.model.TaskStatus;
import com.taskmanagement.model.dto.CommentRequest;
import com.taskmanagement.model.dto.TaskRequest;
import com.taskmanagement.model.dto.TaskUpdateRequest;
import com.taskmanagement.model.dto.UpdateStatusRequest;
import com.taskmanagement.model.entity.Task;
import com.taskmanagement.model.entity.User;
import com.taskmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Tag(name = "User Management", description = "Operations related to user management and tasks")
@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Operation(
      summary = "Get all users",
      description = "Retrieve a list of all users with author role")
  @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
  @GetMapping()
  public ResponseEntity<List<User>> getAllAuthors() {
    return new ResponseEntity<>(userService.getAllAuthors(), HttpStatus.OK);
  }

  @Operation(summary = "Create a new task", description = "Create a new task for the authors")
  @ApiResponse(responseCode = "200", description = "Task created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid task request")
  @PostMapping("/tasks")
  public ResponseEntity<String> createTask(
      @Valid @RequestBody TaskRequest taskRequest, HttpServletRequest request) {
    userService.createTask(request, taskRequest);
    return ResponseEntity.ok("Task was successfully added");
  }

  @Operation(summary = "Delete a task", description = "Delete an existing task by its ID")
  @ApiResponse(responseCode = "200", description = "Task deleted successfully")
  @ApiResponse(responseCode = "404", description = "Task not found")
  @DeleteMapping("/tasks/{taskId}")
  public ResponseEntity<String> deleteTask(
      @Parameter(description = "ID of the task to be deleted") @PathVariable Long taskId,
      HttpServletRequest request) {

    userService.deleteTask(request, taskId);
    return ResponseEntity.ok("Task was successfully deleted");
  }

  @Operation(
      summary = "Get tasks for the authenticated user",
      description = "Retrieve a list of tasks for the authenticated user")
  @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully")
  @GetMapping("/tasks")
  public ResponseEntity<List<Task>> getUserTasks(HttpServletRequest request) {
    return ResponseEntity.ok(userService.getUserTasks(request));
  }

  @Operation(
      summary = "Get tasks by user ID",
      description = "Retrieve tasks for a specific user with optional filters")
  @ApiResponse(responseCode = "200", description = "List of tasks retrieved successfully")
  @ApiResponse(responseCode = "404", description = "User not found")
  @GetMapping("/tasks/user/{userId}")
  public ResponseEntity<Page<Task>> getTasksByUserId(
      @Parameter(description = "ID of the user whose tasks are to be retrieved") @PathVariable
          Long userId,
      @Parameter(description = "Page number for pagination") @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Number of tasks per page") @RequestParam(defaultValue = "10")
          int size,
      @Parameter(description = "Filter by task status") @RequestParam(required = false)
          TaskStatus status,
      @Parameter(description = "Filter by task priority") @RequestParam(required = false)
          TaskPriority priority) {

    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(
        userService.getUserTasksByIdWithFilters(userId, pageable, status, priority));
  }

  @Operation(
      summary = "Add performer to a task",
      description = "Assign a performer to a specific task")
  @ApiResponse(responseCode = "200", description = "Performer added successfully")
  @ApiResponse(responseCode = "404", description = "Task or performer not found")
  @PostMapping("/tasks/{taskId}/performers/{performerId}")
  public ResponseEntity<String> addPerformer(
      @Parameter(description = "ID of the task to which the performer will be added") @PathVariable
          Long taskId,
      @Parameter(description = "ID of the performer to be assigned") @PathVariable
          Long performerId) {
    userService.addPerformer(taskId, performerId);
    return ResponseEntity.ok("Performer was successfully added");
  }

  @Operation(summary = "Add comment to a task", description = "Add a comment to a specific task")
  @ApiResponse(responseCode = "200", description = "Comment added successfully")
  @ApiResponse(responseCode = "404", description = "Task not found")
  @PostMapping("/tasks/comments/{taskId}")
  public ResponseEntity<String> addComment(
      @Parameter(description = "ID of the task to which the comment will be added") @PathVariable
          Long taskId,
      @Valid @RequestBody CommentRequest commentRequest,
      HttpServletRequest request) {
    userService.addCommentToTask(taskId, commentRequest.getComment(), request);
    return ResponseEntity.ok("Comment was successfully added");
  }

  @Operation(summary = "Update a task", description = "Update details of an existing task")
  @ApiResponse(responseCode = "200", description = "Task updated successfully")
  @ApiResponse(responseCode = "404", description = "Task not found")
  @PatchMapping("/tasks/edit/{taskId}")
  public ResponseEntity<String> updateTask(
      @Parameter(description = "ID of the task to be updated") @PathVariable Long taskId,
      @RequestBody TaskUpdateRequest updateRequest,
      HttpServletRequest request) {
    userService.updateTask(taskId, updateRequest, request);
    return ResponseEntity.ok("Task was successfully updated");
  }

  @Operation(summary = "Update task status", description = "Update the status of a specific task")
  @ApiResponse(responseCode = "200", description = "Status updated successfully")
  @ApiResponse(responseCode = "404", description = "Task not found")
  @PatchMapping("/tasks/status/{taskId}")
  public ResponseEntity<String> updateStatus(
      @RequestBody UpdateStatusRequest updateStatusRequest,
      @Parameter(description = "ID of the task to update the status") @PathVariable Long taskId,
      HttpServletRequest httpServletRequest) {
    userService.updateStatus(updateStatusRequest, taskId, httpServletRequest);
    return ResponseEntity.ok("Status was successfully updated");
  }

  @Operation(
      summary = "Get comments by task ID",
      description = "Retrieve all comments for a specific task")
  @ApiResponse(responseCode = "200", description = "List of comments retrieved successfully")
  @ApiResponse(responseCode = "404", description = "Task not found")
  @GetMapping("/tasks/comments/{taskId}")
  public ResponseEntity<List<Comment>> addComment(
      @Parameter(description = "ID of the task whose comments are to be retrieved") @PathVariable
          Long taskId) {
    return ResponseEntity.ok(userService.getCommentsByTaskId(taskId));
  }
}
