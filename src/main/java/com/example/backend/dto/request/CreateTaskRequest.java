package com.example.backend.dto.request;

import com.example.backend.entity.Task.Priority;
import com.example.backend.entity.Task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    private String description;

    private TaskStatus status;

    private Priority priority;

    private LocalDate dueDate;

    private LocalTime dueTime;

    @NotNull(message = "userId is required")
    private Long userId;
}
