package com.intern.fwork.controllers;

import com.intern.fwork.dtos.request.UpdateWorkspaceRequest;
import com.intern.fwork.dtos.request.UpdateBoardRequest;
import com.intern.fwork.dtos.response.BoardResponse;
import com.intern.fwork.dtos.response.WorkspaceCacheDto;
import com.intern.fwork.dtos.response.WorkspaceResponse;
import com.intern.fwork.entities.User;
import com.intern.fwork.entities.Workspace;
import com.intern.fwork.entities.WorkspaceMember;
import com.intern.fwork.entities.Board;
import com.intern.fwork.enums.Role;
import com.intern.fwork.enums.WorkspaceRole;
import com.intern.fwork.repositories.WorkspaceRepository;
import com.intern.fwork.repositories.UserRepository;
import com.intern.fwork.repositories.WorkspaceMemberRepository;
import com.intern.fwork.repositories.BoardRepository;
import com.intern.fwork.services.WorkspaceService;
import com.intern.fwork.services.BoardService;
import com.intern.fwork.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CacheIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private BoardRepository boardRepository;

    private User ownerUser;
    private Workspace workspace;
    private Board board;

    @BeforeEach
    public void setup() {
        // Clear caches to start clean
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());

        // Create Owner User
        ownerUser = User.builder()
                .name("Owner User")
                .email("owner_cache@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        ownerUser = userRepository.save(ownerUser);

        // Authenticate User
        CustomUserDetails userDetails = new CustomUserDetails(ownerUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );

        // Create Workspace
        workspace = Workspace.builder()
                .name("Cache Workspace")
                .slug("cache-workspace")
                .description("Desc")
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .build();
        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(ownerUser)
                .role(WorkspaceRole.OWNER)
                .build();
        workspaceMemberRepository.save(member);

        // Create Board
        board = Board.builder()
                .title("Cache Board")
                .description("Desc")
                .color("#FFFFFF")
                .workspace(workspace)
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .isArchived(false)
                .position(0)
                .build();
        board = boardRepository.save(board);
    }

    @Test
    public void testWorkspaceCachingAndEviction() {
        UUID workspaceId = workspace.getId();

        // 1. Initial State: Cache should be empty (or null value)
        assertNull(cacheManager.getCache("workspace").get(workspaceId));

        // 2. First call to getById: Should load from DB and populate Cache
        WorkspaceResponse response1 = workspaceService.getById(workspaceId);
        assertNotNull(response1);

        WorkspaceCacheDto cachedWorkspace = cacheManager.getCache("workspace").get(workspaceId, WorkspaceCacheDto.class);
        assertNotNull(cachedWorkspace);
        assertEquals(workspace.getName(), cachedWorkspace.getName());

        // 3. Update Workspace: Should evict Cache
        UpdateWorkspaceRequest updateRequest = new UpdateWorkspaceRequest();
        updateRequest.setName("Updated Cache Workspace");
        updateRequest.setDescription("New Desc");
        workspaceService.update(workspaceId, updateRequest);

        assertNull(cacheManager.getCache("workspace").get(workspaceId));

        // 4. Second call to getById: Should populate Cache again
        WorkspaceResponse response2 = workspaceService.getById(workspaceId);
        assertEquals("Updated Cache Workspace", response2.getName());
        assertNotNull(cacheManager.getCache("workspace").get(workspaceId));
    }

    @Test
    public void testBoardCachingAndEviction() {
        UUID boardId = board.getId();

        // 1. Initial State: Cache should be empty
        assertNull(cacheManager.getCache("board").get(boardId));

        // 2. First call: Should load from DB and populate Cache
        BoardResponse response1 = boardService.getById(boardId);
        assertNotNull(response1);

        BoardResponse cachedBoard = cacheManager.getCache("board").get(boardId, BoardResponse.class);
        assertNotNull(cachedBoard);
        assertEquals(board.getTitle(), cachedBoard.getTitle());

        // 3. Update Board: Should evict Cache
        UpdateBoardRequest updateRequest = new UpdateBoardRequest();
        updateRequest.setTitle("Updated Cache Board");
        updateRequest.setDescription("New Desc");
        updateRequest.setColor("#000000");
        boardService.update(boardId, updateRequest);

        assertNull(cacheManager.getCache("board").get(boardId));
    }
}
