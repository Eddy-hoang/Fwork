package com.intern.fwork.repositories;

import com.intern.fwork.entities.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
    List<WorkspaceMember> findByUserId(UUID userId);
    List<WorkspaceMember> findByWorkspaceId(UUID workspaceId);
    long countByWorkspaceId(UUID workspaceId);
    boolean existsByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    /**
     * Batch member count for multiple workspaces in a single GROUP BY query.
     * Used by getMyWorkspaces() to eliminate N+1 count queries.
     * Returns a list of [workspaceId, memberCount] pairs.
     */
    @Query("SELECT m.workspace.id, COUNT(m) FROM WorkspaceMember m WHERE m.workspace.id IN :workspaceIds GROUP BY m.workspace.id")
    List<Object[]> countMembersByWorkspaceIds(@Param("workspaceIds") List<UUID> workspaceIds);
}
