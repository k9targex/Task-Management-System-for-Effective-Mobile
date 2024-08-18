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
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

  private final UserService userService;




  public UserController(UserService userService) {
    this.userService = userService;
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
  @DeleteMapping("/tasks/{taskId}")
  public ResponseEntity<String> deleteTask(@PathVariable Long taskId, HttpServletRequest request){
    userService.deleteTask(request,taskId);
    return ResponseEntity.ok("Task was successfully deleted");
  }

  @GetMapping("/tasks")
  public ResponseEntity<List<Task>> getUserTasks(HttpServletRequest request){
    return ResponseEntity.ok(userService.getUserTasks(request));
  }
@GetMapping("/tasks/user/{userId}")
public ResponseEntity<Page<Task>> getTasksByUserId(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) TaskStatus status,
        @RequestParam(required = false) TaskPriority priority) {

  Pageable pageable = PageRequest.of(page, size);
  return ResponseEntity.ok(userService.getUserTasksByIdWithFilters(userId, pageable, status, priority));
}

  @PostMapping("/tasks/{taskId}/performers/{performerId}")
  public ResponseEntity<String> addPerformer(@PathVariable Long taskId,@PathVariable Long performerId){
    userService.addPerformer(taskId,performerId);
    return ResponseEntity.ok("Performer was successfully added");
  }
  @PostMapping("/tasks/comments/{taskId}")
  public ResponseEntity<String> addComment( @PathVariable Long taskId, @Valid @RequestBody CommentRequest commentRequest, HttpServletRequest request){
    userService.addCommentToTask(taskId,commentRequest.getComment(),request);
    return ResponseEntity.ok("Comment was successfully added");
  }
  @PatchMapping("/tasks/edit/{taskId}")
  public ResponseEntity<String> updateTask(@PathVariable Long taskId, @RequestBody TaskUpdateRequest updateRequest, HttpServletRequest request){
    userService.updateTask(taskId,updateRequest,request);
    return ResponseEntity.ok("Task was successfully updated");
  }
  @PatchMapping("/tasks/status/{taskId}")
  public ResponseEntity<String> updateStatus( @RequestBody UpdateStatusRequest updateStatusRequest, @PathVariable Long taskId,HttpServletRequest httpServletRequest)
  {
    userService.updateStatus(updateStatusRequest,taskId,httpServletRequest);
    return ResponseEntity.ok("Status was successfully updated");
  }

  @GetMapping("/tasks/comments/{taskId}")
  public ResponseEntity<List<Comment>> addComment(@PathVariable Long taskId){
    return ResponseEntity.ok(userService.getCommentsByTaskId(taskId));
  }



}
