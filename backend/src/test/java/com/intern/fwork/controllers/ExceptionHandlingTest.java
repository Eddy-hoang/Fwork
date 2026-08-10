package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.CreateTaskRequest;
import com.intern.fwork.dtos.request.MoveTaskRequest;
import com.intern.fwork.entities.*;
import com.intern.fwork.enums.Role;
import com.intern.fwork.enums.WorkspaceRole;
import com.intern.fwork.repositories.*;
import com.intern.fwork.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class ExceptionHandlingTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardColumnRepository boardColumnRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private Authentication currentAuth;

    private User ownerUser;
    private User memberUser;
    private Workspace workspace;
    private Board board;
    private BoardColumn column;
    private Task task;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Clear repositories
        taskRepository.deleteAll();
        boardColumnRepository.deleteAll();
        boardRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();

        // Create Users
        ownerUser = User.builder()
                .name("Owner User")
                .email("owner@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        ownerUser = userRepository.save(ownerUser);

        memberUser = User.builder()
                .name("Member User")
                .email("member@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        memberUser = userRepository.save(memberUser);

        // Create Workspace
        workspace = Workspace.builder()
                .name("Test Workspace")
                .slug("test-workspace")
                .description("Workspace for integration testing")
                .createdBy(ownerUser.getId())
                .build();
        workspace = workspaceRepository.save(workspace);

        // Add Members
        WorkspaceMember ownerMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(ownerUser)
                .role(WorkspaceRole.OWNER)
                .build();
        workspaceMemberRepository.save(ownerMember);

        WorkspaceMember normalMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(memberUser)
                .role(WorkspaceRole.MEMBER)
                .build();
        workspaceMemberRepository.save(normalMember);

        // Create Board
        board = Board.builder()
                .title("Kanban Board")
                .description("Development Kanban Board")
                .color("#000000")
                .workspace(workspace)
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .isArchived(false)
                .position(0)
                .build();
        board = boardRepository.save(board);

        // Create Column
        column = BoardColumn.builder()
                .name("TODO")
                .position(0)
                .board(board)
                .build();
        column = boardColumnRepository.save(column);

        // Create Task
        task = Task.builder()
                .title("Task")
                .position(0)
                .column(column)
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .isArchived(false)
                .build();
        task = taskRepository.save(task);
    }

    private void authenticate(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        currentAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(currentAuth);
    }

    @Test
    public void testResourceNotFound_404() throws Exception {
        authenticate(ownerUser);
        mockMvc.perform(get("/api/tasks/" + UUID.randomUUID())
                        .with(authentication(currentAuth)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Task not found")));
    }

    @Test
    public void testAccessDenied_403() throws Exception {
        authenticate(memberUser);
        mockMvc.perform(delete("/api/tasks/" + task.getId())
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.message", containsString("Only OWNER or ADMIN can delete tasks")));
    }

    @Test
    public void testValidationFail_400() throws Exception {
        authenticate(ownerUser);
        CreateTaskRequest invalidRequest = new CreateTaskRequest();
        invalidRequest.setTitle(""); // Trigger @NotBlank validation error
        invalidRequest.setPosition(0);

        mockMvc.perform(post("/api/columns/" + column.getId() + "/tasks")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.errors.title", is("Task title is required")));
    }

    @Test
    public void testUnauthorized_401() throws Exception {
        // Request secure endpoint without authentication details -> Spring Security triggers AuthenticationException
        mockMvc.perform(get("/api/tasks/" + task.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    public void testValidationFail_PositionNegative() throws Exception {
        authenticate(ownerUser);
        CreateTaskRequest invalidRequest = new CreateTaskRequest();
        invalidRequest.setTitle("Valid Title");
        invalidRequest.setPosition(-5); // Negative position

        mockMvc.perform(post("/api/columns/" + column.getId() + "/tasks")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.position", is("Position must be >= 0")));
    }

    @Test
    public void testMoveTask_PositionNegative() throws Exception {
        authenticate(ownerUser);
        MoveTaskRequest invalidRequest = new MoveTaskRequest();
        invalidRequest.setTargetColumnId(column.getId());
        invalidRequest.setTargetPosition(-1); // Negative position

        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/move")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.targetPosition", is("Target position must be >= 0")));
    }

    @Test
    public void testMoveTask_PositionClamp() throws Exception {
        authenticate(ownerUser);
        MoveTaskRequest clampRequest = new MoveTaskRequest();
        clampRequest.setTargetColumnId(column.getId());
        clampRequest.setTargetPosition(999999); // Clamp to end of list

        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/move")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clampRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.position", is(0))); // only 1 task in column, so clamped to index 0
    }

    @Test
    public void testValidationFail_TitleTooLong() throws Exception {
        authenticate(ownerUser);
        CreateTaskRequest invalidRequest = new CreateTaskRequest();
        invalidRequest.setTitle("a".repeat(256)); // > 255 chars
        invalidRequest.setPosition(0);

        mockMvc.perform(post("/api/columns/" + column.getId() + "/tasks")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors.title", is("Task title must not exceed 255 characters")));
    }

    @Test
    public void testUnauthorized_InvalidJWT_401() throws Exception {
        // Request secure endpoint with invalid/malformed Bearer token -> returns HTTP 401 Unauthorized
        mockMvc.perform(get("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer invalidjwttokenpayload"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }
}
