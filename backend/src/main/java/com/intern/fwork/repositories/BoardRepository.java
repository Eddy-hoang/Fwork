package com.intern.fwork.repositories;

import com.intern.fwork.entities.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BoardRepository extends JpaRepository<Board, UUID> {
    
    long countByWorkspaceIdAndIsArchivedFalse(UUID workspaceId);

    List<Board> findByWorkspaceIdAndIsArchivedFalseOrderByPositionAsc(UUID workspaceId);

    @Query("SELECT DISTINCT b FROM Board b JOIN FETCH b.workspace w JOIN w.members m WHERE m.user.id = :userId AND b.isArchived = false AND w.isArchived = false")
    List<Board> findAllByMemberUserId(@Param("userId") UUID userId);

    /**
     * Batch board count for multiple workspaces in a single GROUP BY query.
     * Used by getMyWorkspaces() to eliminate N+1 count queries.
     * Returns a list of [workspaceId, boardCount] pairs.
     */
    @Query("SELECT b.workspace.id, COUNT(b) FROM Board b WHERE b.workspace.id IN :workspaceIds AND b.isArchived = false GROUP BY b.workspace.id")
    List<Object[]> countBoardsByWorkspaceIds(@Param("workspaceIds") List<UUID> workspaceIds);
}

