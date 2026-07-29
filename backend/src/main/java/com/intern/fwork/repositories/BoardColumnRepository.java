package com.intern.fwork.repositories;

import com.intern.fwork.entities.BoardColumn;
import com.intern.fwork.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, UUID> {
    // Lấy cột của bảng theo thứ tự tăng
    List<BoardColumn> findByBoardIdOrderByPositionAsc(UUID boardId);
}
