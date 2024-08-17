package com.taskmanagement.service;

import com.taskmanagement.dao.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    @Autowired
    public TaskService(
            TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }


}
