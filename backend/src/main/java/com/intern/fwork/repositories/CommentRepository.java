package com.intern.fwork.repositories;

import com.intern.fwork.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.createdBy WHERE c.task.id = :taskId ORDER BY c.createdAt ASC")
    List<Comment> findByTaskIdOrderByCreatedAtAsc(@Param("taskId") UUID taskId);

    @Query(value = "SELECT c FROM Comment c LEFT JOIN FETCH c.createdBy WHERE c.task.id = :taskId ORDER BY c.createdAt ASC",
           countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.task.id = :taskId")
    Page<Comment> findByTaskIdOrderByCreatedAtAsc(@Param("taskId") UUID taskId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") UUID taskId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.task.column.id = :columnId")
    void deleteByColumnId(@Param("columnId") UUID columnId);
}
