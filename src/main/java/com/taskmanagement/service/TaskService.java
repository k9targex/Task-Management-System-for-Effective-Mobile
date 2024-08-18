package com.taskmanagement.service;

import com.taskmanagement.dao.TaskRepository;
import com.taskmanagement.model.entity.Task;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TaskService {
  private final TaskRepository taskRepository;

  @Autowired
  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public List<Task> getTasks() {
    return taskRepository.findAll();
  }
}
