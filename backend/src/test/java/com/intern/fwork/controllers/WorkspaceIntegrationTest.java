package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.AddMemberRequest;
import com.intern.fwork.dtos.request.CreateWorkspaceRequest;
import com.intern.fwork.dtos.request.UpdateWorkspaceRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class WorkspaceIntegrationTest {

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Authentication currentAuth;

    private User ownerUser;
    private User collaboratorUser;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        taskRepository.deleteAll();
        boardColumnRepository.deleteAll();
        boardRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        workspaceRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Create Owner
        ownerUser = User.builder()
                .name("Owner User")
                .email("owner@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        ownerUser = userRepository.save(ownerUser);

        // Create Collaborator
        collaboratorUser = User.builder()
                .name("Collaborator User")
                .email("collaborator@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        collaboratorUser = userRepository.save(collaboratorUser);
    }

    private void authenticate(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        currentAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(currentAuth);
    }

    @Test
    public void testWorkspaceCRUDAndMembers() throws Exception {
        authenticate(ownerUser);

        // --- 1. CREATE WORKSPACE ---
        CreateWorkspaceRequest createRequest = new CreateWorkspaceRequest();
        createRequest.setName("Integration Workspace");
        createRequest.setDescription("Workspace Description");

        String createRes = mockMvc.perform(post("/api/workspaces")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Integration Workspace")))
                .andExpect(jsonPath("$.data.createdBy", is(ownerUser.getId().toString())))
                .andExpect(jsonPath("$.data.currentUserRole", is(WorkspaceRole.OWNER.toString())))
                .andReturn().getResponse().getContentAsString();

        UUID workspaceId = UUID.fromString(objectMapper.readTree(createRes).path("data").path("id").asText());

        // --- 2. GET WORKSPACES ---
        mockMvc.perform(get("/api/workspaces")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(workspaceId.toString())));

        // --- 3. GET WORKSPACE DETAIL ---
        mockMvc.perform(get("/api/workspaces/" + workspaceId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Integration Workspace")));

        // --- 4. UPDATE WORKSPACE ---
        UpdateWorkspaceRequest updateRequest = new UpdateWorkspaceRequest();
        updateRequest.setName("Workspace Updated");
        updateRequest.setDescription("New Desc");

        mockMvc.perform(put("/api/workspaces/" + workspaceId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Workspace Updated")));

        // --- 5. ADD MEMBER ---
        AddMemberRequest addMemberReq = new AddMemberRequest();
        addMemberReq.setEmail(collaboratorUser.getEmail());
        addMemberReq.setRole(WorkspaceRole.MEMBER);

        mockMvc.perform(post("/api/workspaces/" + workspaceId + "/members")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addMemberReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)));

        // Try adding duplicate member -> Expected 409 Conflict
        mockMvc.perform(post("/api/workspaces/" + workspaceId + "/members")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addMemberReq)))
                .andExpect(status().isConflict());

        // --- 6. GET MEMBERS ---
        mockMvc.perform(get("/api/workspaces/" + workspaceId + "/members")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].email", hasItems(ownerUser.getEmail(), collaboratorUser.getEmail())));

        // --- 7. REMOVE MEMBER ---
        mockMvc.perform(delete("/api/workspaces/" + workspaceId + "/members/" + collaboratorUser.getId())
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // --- 8. DELETE WORKSPACE ---
        mockMvc.perform(delete("/api/workspaces/" + workspaceId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Look up deleted workspace -> Expected 404 Not Found
        mockMvc.perform(get("/api/workspaces/" + workspaceId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isNotFound());
    }
}
