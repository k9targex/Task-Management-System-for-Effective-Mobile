package com.taskmanagement.model.dto;

import com.taskmanagement.model.TaskPriority;
import com.taskmanagement.model.TaskStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
}