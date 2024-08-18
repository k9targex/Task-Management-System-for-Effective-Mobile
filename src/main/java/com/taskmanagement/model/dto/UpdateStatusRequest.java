package com.taskmanagement.model.dto;

import com.taskmanagement.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {
    @NotNull(message = "Status is required")
    private TaskStatus status;
}
