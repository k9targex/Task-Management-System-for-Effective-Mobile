package com.taskmanagement.model.dto;

import com.taskmanagement.model.TaskPriority;
import com.taskmanagement.model.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TaskUpdateRequest {
  private String title;

  @Size(max = 100, message = ("Description is to big"))
  private String description;

  private TaskStatus status;

  private TaskPriority priority;
}
