package com.example.backend.service.impl;

import com.example.backend.dto.request.CreateTaskRequest;
import com.example.backend.dto.request.UpdateTaskRequest;
import com.example.backend.dto.response.TaskResponse;
import com.example.backend.entity.Task;
import com.example.backend.entity.Task.Priority;
import com.example.backend.entity.Task.TaskStatus;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.TaskService;
import com.example.backend.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getUserId()));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .dueDate(request.getDueDate())
                .dueTime(request.getDueTime())
                .user(user)
                .build();

        return mapToTaskResponse(taskRepository.save(task));
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        return mapToTaskResponse(findTaskOrThrow(id));
    }

    @Override
    public List<TaskResponse> getAllTasks(TaskStatus status, Priority priority) {
        Specification<Task> spec = Specification
                .where(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasPriority(priority));
        return taskRepository.findAll(spec)
                .stream()
                .map(this::mapToTaskResponse)
                .toList();
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        Task task = findTaskOrThrow(id);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setDueTime(request.getDueTime());
        return mapToTaskResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .dueTime(task.getDueTime())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .userId(task.getUser().getId())
                .userName(task.getUser().getName())
                .build();
    }
}
