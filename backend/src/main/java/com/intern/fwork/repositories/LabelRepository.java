package com.intern.fwork.repositories;

import com.intern.fwork.entities.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LabelRepository extends JpaRepository<Label, UUID> {

    List<Label> findByBoardId(UUID boardId);

    boolean existsByBoardIdAndName(UUID boardId, String name);

    boolean existsByBoardIdAndNameAndIdNot(UUID boardId, String name, UUID id);

}
