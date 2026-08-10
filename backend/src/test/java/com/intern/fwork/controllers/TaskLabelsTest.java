package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.CreateLabelRequest;
import com.intern.fwork.dtos.request.TaskLabelsRequest;
import com.intern.fwork.dtos.request.UpdateLabelRequest;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class TaskLabelsTest {

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
    private LabelRepository labelRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Authentication currentAuth;

    private User ownerUser;
    private User adminUser;
    private User memberUser;
    private Workspace workspace;
    private Board boardA;
    private Board boardB;
    private Task taskA;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        taskRepository.deleteAll();
        labelRepository.deleteAll();
        boardColumnRepository.deleteAll();
        boardRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        workspaceRepository.deleteAll();
        refreshTokenRepository.deleteAll();
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

        // Create Workspace
        workspace = Workspace.builder()
                .name("Workspace")
                .slug("test-workspace")
                .description("Workspace description")
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .build();
        workspace = workspaceRepository.save(workspace);

        // Add Members
        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace)
                .user(ownerUser)
                .role(WorkspaceRole.OWNER)
                .build());

        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace)
                .user(adminUser)
                .role(WorkspaceRole.ADMIN)
                .build());

        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace)
                .user(memberUser)
                .role(WorkspaceRole.MEMBER)
                .build());

        // Create Board A
        boardA = Board.builder()
                .title("Board A")
                .workspace(workspace)
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .build();
        boardA = boardRepository.save(boardA);

        // Create Board B
        boardB = Board.builder()
                .title("Board B")
                .workspace(workspace)
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .build();
        boardB = boardRepository.save(boardB);

        // Create Column in Board A
        BoardColumn colA = BoardColumn.builder()
                .name("TODO")
                .position(0)
                .board(boardA)
                .build();
        colA = boardColumnRepository.save(colA);

        // Create Task in Board A
        taskA = Task.builder()
                .title("Task A")
                .position(0)
                .column(colA)
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .build();
        taskA = taskRepository.save(taskA);
    }

    private void authenticate(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        currentAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(currentAuth);
    }

    @Test
    public void testTaskLabelsManagementFlow() throws Exception {
        CreateLabelRequest createRequest = new CreateLabelRequest("Backend", "#FF0000");

        // 1. MEMBER tries to create label on Board A -> Expected 403 Forbidden
        authenticate(memberUser);
        mockMvc.perform(post("/api/boards/" + boardA.getId() + "/labels")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        // 2. ADMIN creates label on Board A -> Expected 201 Created
        authenticate(adminUser);
        String labelARes = mockMvc.perform(post("/api/boards/" + boardA.getId() + "/labels")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Backend")))
                .andExpect(jsonPath("$.data.color", is("#FF0000")))
                .andExpect(jsonPath("$.data.boardId", is(boardA.getId().toString())))
                .andReturn().getResponse().getContentAsString();
        UUID labelAId = UUID.fromString(objectMapper.readTree(labelARes).path("data").path("id").asText());

        // Try creating label with duplicate name on Board A -> Expected 409 Conflict
        mockMvc.perform(post("/api/boards/" + boardA.getId() + "/labels")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());

        // 3. OWNER creates label on Board B -> Expected 201 Created
        authenticate(ownerUser);
        createRequest.setName("Frontend");
        createRequest.setColor("#00FF00");
        String labelBRes = mockMvc.perform(post("/api/boards/" + boardB.getId() + "/labels")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Frontend")))
                .andExpect(jsonPath("$.data.boardId", is(boardB.getId().toString())))
                .andReturn().getResponse().getContentAsString();
        UUID labelBId = UUID.fromString(objectMapper.readTree(labelBRes).path("data").path("id").asText());

        // 4. GET Labels of Board A -> Expected 200 OK (contains "Backend")
        authenticate(memberUser);
        mockMvc.perform(get("/api/boards/" + boardA.getId() + "/labels")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(labelAId.toString())));

        // 5. UPDATE Label -> Expected 200 OK
        authenticate(adminUser);
        UpdateLabelRequest updateRequest = new UpdateLabelRequest("Database", "#0000FF");
        mockMvc.perform(put("/api/labels/" + labelAId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Database")))
                .andExpect(jsonPath("$.data.color", is("#0000FF")));

        // 6. MEMBER updates labels for Task A (adding "Database" label) -> Expected 200 OK
        authenticate(memberUser);
        TaskLabelsRequest taskLabelsRequest = new TaskLabelsRequest(List.of(labelAId));
        mockMvc.perform(put("/api/tasks/" + taskA.getId() + "/labels")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskLabelsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.labels", hasSize(1)))
                .andExpect(jsonPath("$.data.labels[0].id", is(labelAId.toString())));

        // 7. Try assigning label of Board B ("Frontend") to Task of Board A -> Expected 400 Bad Request
        taskLabelsRequest.setLabelIds(List.of(labelBId));
        mockMvc.perform(put("/api/tasks/" + taskA.getId() + "/labels")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskLabelsRequest)))
                .andExpect(status().isBadRequest());

        // 8. Try assigning non-existent label UUID to Task -> Expected 404 Not Found
        taskLabelsRequest.setLabelIds(List.of(UUID.randomUUID()));
        mockMvc.perform(put("/api/tasks/" + taskA.getId() + "/labels")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskLabelsRequest)))
                .andExpect(status().isNotFound());

        // 9. DELETE Label from Board A -> Expected 200 OK
        authenticate(adminUser);
        mockMvc.perform(delete("/api/labels/" + labelAId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());

        // Verify task A labels list is now empty
        authenticate(memberUser);
        mockMvc.perform(get("/api/tasks/" + taskA.getId())
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.labels", hasSize(0)));
    }
}
