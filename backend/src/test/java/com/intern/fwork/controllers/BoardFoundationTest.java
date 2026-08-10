package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.CreateBoardRequest;
import com.intern.fwork.dtos.request.UpdateBoardRequest;
import com.intern.fwork.entities.User;
import com.intern.fwork.entities.Workspace;
import com.intern.fwork.entities.WorkspaceMember;
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
public class BoardFoundationTest {

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
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BoardColumnRepository boardColumnRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private Authentication currentAuth;

    private User ownerUser;
    private User adminUser;
    private User memberUser;
    private User externalUser;
    private Workspace workspace;

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
    }

    private void authenticate(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        currentAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(currentAuth);
    }

    @Test
    public void testBoardFoundationWorkflow() throws Exception {
        // --- 1. CREATE BOARD MATRIX ---
        
        // OWNER creates Board -> Expected 201 Created
        authenticate(ownerUser);
        CreateBoardRequest createRequest = new CreateBoardRequest();
        createRequest.setTitle("Sprint Board OWNER");
        createRequest.setDescription("Sprint board for development");
        createRequest.setColor("#111111");
        createRequest.setWorkspaceId(workspace.getId());

        String resOwner = mockMvc.perform(post("/api/boards")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Sprint Board OWNER")))
                .andExpect(jsonPath("$.data.createdBy", is(ownerUser.getId().toString())))
                .andExpect(jsonPath("$.data.updatedBy", is(ownerUser.getId().toString())))
                .andReturn().getResponse().getContentAsString();
        UUID ownerBoardId = UUID.fromString(objectMapper.readTree(resOwner).path("data").path("id").asText());

        // ADMIN creates Board -> Expected 201 Created
        authenticate(adminUser);
        createRequest.setTitle("Sprint Board ADMIN");
        createRequest.setColor("#222222");
        String resAdmin = mockMvc.perform(post("/api/boards")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Sprint Board ADMIN")))
                .andExpect(jsonPath("$.data.createdBy", is(adminUser.getId().toString())))
                .andReturn().getResponse().getContentAsString();
        UUID adminBoardId = UUID.fromString(objectMapper.readTree(resAdmin).path("data").path("id").asText());

        // MEMBER creates Board -> Expected 201 Created
        authenticate(memberUser);
        createRequest.setTitle("Sprint Board MEMBER");
        createRequest.setColor("#333333");
        String resMember = mockMvc.perform(post("/api/boards")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Sprint Board MEMBER")))
                .andExpect(jsonPath("$.data.createdBy", is(memberUser.getId().toString())))
                .andReturn().getResponse().getContentAsString();
        UUID memberBoardId = UUID.fromString(objectMapper.readTree(resMember).path("data").path("id").asText());

        // Non-member creates Board -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(post("/api/boards")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());


        // --- 2. GET BOARD (DETAIL) MATRIX ---
        
        // OWNER gets Board -> Expected 200 OK
        authenticate(ownerUser);
        mockMvc.perform(get("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(memberBoardId.toString())));

        // ADMIN gets Board -> Expected 200 OK
        authenticate(adminUser);
        mockMvc.perform(get("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // MEMBER gets Board -> Expected 200 OK
        authenticate(memberUser);
        mockMvc.perform(get("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Non-member gets Board -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(get("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());


        // --- 3. UPDATE BOARD MATRIX ---
        
        UpdateBoardRequest updateRequest = new UpdateBoardRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated desc");
        updateRequest.setColor("#999999");

        // OWNER updates Board -> Expected 200 OK
        authenticate(ownerUser);
        mockMvc.perform(put("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Updated Title")))
                .andExpect(jsonPath("$.data.updatedBy", is(ownerUser.getId().toString())));

        // ADMIN updates Board -> Expected 200 OK
        authenticate(adminUser);
        updateRequest.setTitle("Updated Title By Admin");
        mockMvc.perform(put("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.title", is("Updated Title By Admin")))
                .andExpect(jsonPath("$.data.updatedBy", is(adminUser.getId().toString())));

        // MEMBER updates Board -> Expected 403 Forbidden
        authenticate(memberUser);
        mockMvc.perform(put("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        // Non-member updates Board -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(put("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());


        // --- 4. DELETE BOARD MATRIX ---
        
        // MEMBER deletes Board -> Expected 403 Forbidden
        authenticate(memberUser);
        mockMvc.perform(delete("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());

        // Non-member deletes Board -> Expected 403 Forbidden
        authenticate(externalUser);
        mockMvc.perform(delete("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());

        // ADMIN deletes Board -> Expected 200 OK
        authenticate(adminUser);
        mockMvc.perform(delete("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());

        // Verify deleted board detail lookup now returns 404 (archived filter)
        authenticate(ownerUser);
        mockMvc.perform(get("/api/boards/" + memberBoardId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isNotFound());

        // OWNER deletes Board -> Expected 200 OK
        authenticate(ownerUser);
        mockMvc.perform(delete("/api/boards/" + adminBoardId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());


        // --- 5. GET MY BOARDS MATRIX ---
        
        // Owner sees only unarchived boards (should be ownerBoardId since adminBoardId and memberBoardId are archived/deleted)
        authenticate(ownerUser);
        mockMvc.perform(get("/api/boards")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(ownerBoardId.toString())));

        // External user sees 0 boards
        authenticate(externalUser);
        mockMvc.perform(get("/api/boards")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
                
        // Board not found -> Expected 404
        authenticate(ownerUser);
        mockMvc.perform(get("/api/boards/" + UUID.randomUUID())
                        .with(authentication(currentAuth)))
                .andExpect(status().isNotFound());
    }
}
