package com.example.backend.service;

import com.example.backend.dto.request.CreateTaskRequest;
import com.example.backend.dto.request.UpdateTaskRequest;
import com.example.backend.dto.response.TaskResponse;
import com.example.backend.entity.Task.Priority;
import com.example.backend.entity.Task.TaskStatus;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest request);
    TaskResponse getTaskById(Long id);
    List<TaskResponse> getAllTasks(TaskStatus status, Priority priority);
    TaskResponse updateTask(Long id, UpdateTaskRequest request);
    void deleteTask(Long id);
}
