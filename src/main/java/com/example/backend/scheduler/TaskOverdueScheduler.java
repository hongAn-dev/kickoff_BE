package com.example.backend.scheduler;

import com.example.backend.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskOverdueScheduler {

    private final TaskRepository taskRepository;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void markOverdueTasks() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        int byDate = taskRepository.markOverdueTasksByDate(today);
        int byTime = taskRepository.markOverdueTasksByDateTime(today, now);

        int total = byDate + byTime;
        if (total > 0) {
            log.info("Overdue check: {} task(s) marked as OVERDUE", total);
        }
    }
}
