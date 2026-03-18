package com.example.backend.repository;

import com.example.backend.entity.Task;
import com.example.backend.entity.Task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findByUserId(Long userId);
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);
    boolean existsByIdAndUserId(Long taskId, Long userId);

    @Modifying
    @Query("UPDATE Task t SET t.status = com.example.backend.entity.Task.TaskStatus.OVERDUE " +
           "WHERE t.dueDate < :today " +
           "AND t.status NOT IN (com.example.backend.entity.Task.TaskStatus.DONE, " +
           "com.example.backend.entity.Task.TaskStatus.CANCELLED, " +
           "com.example.backend.entity.Task.TaskStatus.OVERDUE)")
    int markOverdueTasksByDate(@Param("today") LocalDate today);

    @Modifying
    @Query("UPDATE Task t SET t.status = com.example.backend.entity.Task.TaskStatus.OVERDUE " +
           "WHERE t.dueDate = :today AND t.dueTime IS NOT NULL AND t.dueTime <= :now " +
           "AND t.status NOT IN (com.example.backend.entity.Task.TaskStatus.DONE, " +
           "com.example.backend.entity.Task.TaskStatus.CANCELLED, " +
           "com.example.backend.entity.Task.TaskStatus.OVERDUE)")
    int markOverdueTasksByDateTime(@Param("today") LocalDate today, @Param("now") LocalTime now);
}
