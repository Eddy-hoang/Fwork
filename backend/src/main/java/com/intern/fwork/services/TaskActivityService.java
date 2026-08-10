package com.intern.fwork.services;

import com.intern.fwork.dtos.response.TaskActivityResponse;
import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import com.intern.fwork.enums.TaskActivityAction;

import java.util.List;
import java.util.UUID;

public interface TaskActivityService {

    void log(Task task, User actor, TaskActivityAction action, String detail);

    List<TaskActivityResponse> getByTask(UUID taskId);
}
