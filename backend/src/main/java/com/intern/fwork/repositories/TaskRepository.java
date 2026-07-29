package com.intern.fwork.repositories;

import com.intern.fwork.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    // Lấy các task trong một cột, sắp xếp theo thứ tự hiển thị
    List<Task> findByColumnIdOrderByPositionAsc(UUID columnId);
}