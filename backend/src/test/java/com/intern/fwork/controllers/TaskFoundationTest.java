package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.CreateTaskRequest;
import com.intern.fwork.dtos.request.MoveTaskRequest;
import com.intern.fwork.dtos.request.UpdateTaskRequest;
import com.intern.fwork.entities.*;
import com.intern.fwork.enums.Priority;
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
public class TaskFoundationTest {

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
    private User adminUser;
    private User memberUser;
    private User externalUser;
    private Workspace workspace;
    private Board board;
    private BoardColumn columnA;
    private BoardColumn columnB;

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

        adminUser = User.builder()
                .name("Admin User")
                .email("admin@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        adminUser = userRepository.save(adminUser);

        memberUser = User.builder()
                .name("Member User")
                .email("member@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        memberUser = userRepository.save(memberUser);

        externalUser = User.builder()
                .name("External User")
                .email("external@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        externalUser = userRepository.save(externalUser);

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

        WorkspaceMember adminMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(adminUser)
                .role(WorkspaceRole.ADMIN)
                .build();
        workspaceMemberRepository.save(adminMember);

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

        // Create Columns
        columnA = BoardColumn.builder()
                .name("TODO")
                .position(0)
                .board(board)
                .build();
        columnA = boardColumnRepository.save(columnA);

        columnB = BoardColumn.builder()
                .name("IN PROGRESS")
                .position(1)
                .board(board)
                .build();
        columnB = boardColumnRepository.save(columnB);
    }

    private void authenticate(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        currentAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(currentAuth);
    }

    @Test
    public void testTaskFoundationWorkflow() throws Exception {
        // --- 1. CREATE TASK MATRIX ---
        CreateTaskRequest createRequest = new CreateTaskRequest();
        createRequest.setTitle("Task 1");
        createRequest.setDescription("First Task Description");
        createRequest.setPriority(Priority.HIGH);
        createRequest.setPosition(0);

        // MEMBER creates Task -> Expected 201 Created
        authenticate(memberUser);
        String memberRes = mockMvc.perform(post("/api/columns/" + columnA.getId() + "/tasks")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Task 1")))
                .andExpect(jsonPath("$.data.createdBy", is(memberUser.getId().toString())))
                .andReturn().getResponse().getContentAsString();
        UUID task1Id = UUID.fromString(objectMapper.readTree(memberRes).path("data").path("id").asText());

        // ADMIN creates Task -> Expected 201 Created
        authenticate(adminUser);
        createRequest.setTitle("Task 2");
        createRequest.setPosition(1);
        String adminRes = mockMvc.perform(post("/api/columns/" + columnA.getId() + "/tasks")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Task 2")))
                .andReturn().getResponse().getContentAsString();
        UUID task2Id = UUID.fromString(objectMapper.readTree(adminRes).path("data").path("id").asText());

        // Non-member creates Task -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(post("/api/columns/" + columnA.getId() + "/tasks")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());


        // --- 2. GET TASKS BY COLUMN MATRIX ---
        
        // MEMBER gets Column Tasks -> Expected 200 OK (2 items)
        authenticate(memberUser);
        mockMvc.perform(get("/api/columns/" + columnA.getId() + "/tasks")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(task1Id.toString())))
                .andExpect(jsonPath("$.data[1].id", is(task2Id.toString())));

        // External User gets Column Tasks -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(get("/api/columns/" + columnA.getId() + "/tasks")
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());


        // --- 3. GET TASKS BY BOARD MATRIX ---
        
        // MEMBER gets Board Tasks -> Expected 200 OK (2 items)
        authenticate(memberUser);
        mockMvc.perform(get("/api/boards/" + board.getId() + "/tasks")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)));


        // --- 4. GET TASK DETAIL MATRIX ---
        
        // MEMBER gets Detail -> Expected 200 OK
        authenticate(memberUser);
        mockMvc.perform(get("/api/tasks/" + task1Id)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Task 1")));

        // External gets Detail -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(get("/api/tasks/" + task1Id)
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());


        // --- 5. UPDATE TASK MATRIX ---
        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
        updateRequest.setTitle("Task 1 Updated");
        updateRequest.setDescription("New Description");
        updateRequest.setPriority(Priority.LOW);

        // MEMBER updates Task -> Expected 200 OK (MEMBER has edit access to tasks)
        authenticate(memberUser);
        mockMvc.perform(put("/api/tasks/" + task1Id)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Task 1 Updated")))
                .andExpect(jsonPath("$.data.priority", is(Priority.LOW.toString())));

        // External User updates Task -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(put("/api/tasks/" + task1Id)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());


        // --- 6. MOVE TASK SAME COLUMN MATRIX ---
        
        // Current positions in columnA: Task 1 (pos 0), Task 2 (pos 1)
        // Let's move Task 1 to targetPosition 1 (move down below Task 2)
        MoveTaskRequest moveRequest = new MoveTaskRequest();
        moveRequest.setTargetColumnId(columnA.getId());
        moveRequest.setTargetPosition(1);

        authenticate(memberUser);
        mockMvc.perform(patch("/api/tasks/" + task1Id + "/move")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpect(status().isOk());

        // Verify order in columnA: Task 2 should now be pos 0, Task 1 should be pos 1
        mockMvc.perform(get("/api/columns/" + columnA.getId() + "/tasks")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id", is(task2Id.toString())))
                .andExpect(jsonPath("$.data[0].position", is(0)))
                .andExpect(jsonPath("$.data[1].id", is(task1Id.toString())))
                .andExpect(jsonPath("$.data[1].position", is(1)));


        // --- 7. MOVE TASK DIFFERENT COLUMN MATRIX ---
        
        // Let's move Task 2 (TODO column, pos 0) to IN PROGRESS column, pos 0
        moveRequest.setTargetColumnId(columnB.getId());
        moveRequest.setTargetPosition(0);

        mockMvc.perform(patch("/api/tasks/" + task2Id + "/move")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpect(status().isOk());

        // Verify columnA has only Task 1 (reindexed to pos 0)
        mockMvc.perform(get("/api/columns/" + columnA.getId() + "/tasks")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(task1Id.toString())))
                .andExpect(jsonPath("$.data[0].position", is(0)));

        // Verify columnB has Task 2 (pos 0)
        mockMvc.perform(get("/api/columns/" + columnB.getId() + "/tasks")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(task2Id.toString())))
                .andExpect(jsonPath("$.data[0].position", is(0)));


        // --- 8. DELETE (ARCHIVE) TASK MATRIX ---
        
        // MEMBER deletes Task -> Expected 403 Forbidden (Only OWNER/ADMIN delete tasks)
        authenticate(memberUser);
        mockMvc.perform(delete("/api/tasks/" + task1Id)
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());

        // ADMIN deletes Task -> Expected 200 OK
        authenticate(adminUser);
        mockMvc.perform(delete("/api/tasks/" + task1Id)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());

        // Verify lookup on archived task returns 404 (Not Found)
        authenticate(ownerUser);
        mockMvc.perform(get("/api/tasks/" + task1Id)
                        .with(authentication(currentAuth)))
                .andExpect(status().isNotFound());
    }
}
