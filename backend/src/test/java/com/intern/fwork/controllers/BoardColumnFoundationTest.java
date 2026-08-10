package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.CreateBoardColumnRequest;
import com.intern.fwork.dtos.request.UpdateBoardColumnRequest;
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
public class BoardColumnFoundationTest {

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
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TaskRepository taskRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private Authentication currentAuth;

    private User ownerUser;
    private User adminUser;
    private User memberUser;
    private User externalUser;
    private Workspace workspace;
    private Board board;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Clear repositories to start with a clean state (transactional rollback will also clean up)
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
                .createdBy(ownerUser)
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
                .title("Project Board")
                .description("Development Kanban Board")
                .color("#000000")
                .workspace(workspace)
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .isArchived(false)
                .position(0)
                .build();
        board = boardRepository.save(board);
    }

    private void authenticate(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        currentAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(currentAuth);
    }

    @Test
    public void testBoardColumnFoundationWorkflow() throws Exception {
        // --- 1. CREATE COLUMN MATRIX ---
        CreateBoardColumnRequest createRequest = new CreateBoardColumnRequest();
        createRequest.setName("TODO");
        createRequest.setPosition(1);

        // MEMBER creates Column -> Expected 403 Forbidden
        authenticate(memberUser);
        mockMvc.perform(post("/api/boards/" + board.getId() + "/columns")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        // External User creates Column -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(post("/api/boards/" + board.getId() + "/columns")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        // OWNER creates Column -> Expected 201 Created
        authenticate(ownerUser);
        String ownerColRes = mockMvc.perform(post("/api/boards/" + board.getId() + "/columns")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("TODO")))
                .andExpect(jsonPath("$.data.position", is(1)))
                .andReturn().getResponse().getContentAsString();
        UUID ownerColId = UUID.fromString(objectMapper.readTree(ownerColRes).path("data").path("id").asText());

        // ADMIN creates Column -> Expected 201 Created
        authenticate(adminUser);
        createRequest.setName("IN PROGRESS");
        createRequest.setPosition(2);
        String adminColRes = mockMvc.perform(post("/api/boards/" + board.getId() + "/columns")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("IN PROGRESS")))
                .andExpect(jsonPath("$.data.position", is(2)))
                .andReturn().getResponse().getContentAsString();
        UUID adminColId = UUID.fromString(objectMapper.readTree(adminColRes).path("data").path("id").asText());


        // --- 2. GET COLUMNS (LIST) MATRIX ---
        
        // MEMBER gets Columns -> Expected 200 OK (2 items)
        authenticate(memberUser);
        mockMvc.perform(get("/api/boards/" + board.getId() + "/columns")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(ownerColId.toString())))
                .andExpect(jsonPath("$.data[1].id", is(adminColId.toString())));

        // External User gets Columns -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(get("/api/boards/" + board.getId() + "/columns")
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());


        // --- 3. GET COLUMN DETAIL MATRIX ---
        
        // MEMBER gets Column Detail -> Expected 200 OK
        authenticate(memberUser);
        mockMvc.perform(get("/api/columns/" + ownerColId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(ownerColId.toString())));

        // External User gets Column Detail -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(get("/api/columns/" + ownerColId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());


        // --- 4. UPDATE COLUMN MATRIX ---
        UpdateBoardColumnRequest updateRequest = new UpdateBoardColumnRequest();
        updateRequest.setName("TO DO");
        updateRequest.setPosition(0);

        // MEMBER updates Column -> Expected 403 Forbidden
        authenticate(memberUser);
        mockMvc.perform(put("/api/columns/" + ownerColId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        // OWNER updates Column -> Expected 200 OK
        authenticate(ownerUser);
        mockMvc.perform(put("/api/columns/" + ownerColId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("TO DO")))
                .andExpect(jsonPath("$.data.position", is(0)));

        // ADMIN updates Column -> Expected 200 OK
        authenticate(adminUser);
        updateRequest.setName("TO DO UPDATED");
        mockMvc.perform(put("/api/columns/" + ownerColId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("TO DO UPDATED")));


        // --- 5. DELETE COLUMN MATRIX ---
        
        // MEMBER deletes Column -> Expected 403 Forbidden
        authenticate(memberUser);
        mockMvc.perform(delete("/api/columns/" + adminColId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());

        // External User deletes Column -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(delete("/api/columns/" + adminColId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());

        // ADMIN deletes Column -> Expected 200 OK
        authenticate(adminUser);
        mockMvc.perform(delete("/api/columns/" + adminColId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());

        // Verify deleted column detail lookup returns 404
        authenticate(ownerUser);
        mockMvc.perform(get("/api/columns/" + adminColId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isNotFound());

        // OWNER deletes Column -> Expected 200 OK
        authenticate(ownerUser);
        mockMvc.perform(delete("/api/columns/" + ownerColId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());

        // Verify all columns of board are empty now
        mockMvc.perform(get("/api/boards/" + board.getId() + "/columns")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
