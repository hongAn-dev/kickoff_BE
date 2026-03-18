package com.example.backend.specification;

import com.example.backend.entity.Task;
import com.example.backend.entity.Task.Priority;
import com.example.backend.entity.Task.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecification {

    private TaskSpecification() {}

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(Priority priority) {
        return (root, query, cb) ->
                priority == null ? null : cb.equal(root.get("priority"), priority);
    }
}
