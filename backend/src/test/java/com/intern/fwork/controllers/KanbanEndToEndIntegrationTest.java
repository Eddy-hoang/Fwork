package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.*;
import com.intern.fwork.dtos.response.*;
import com.intern.fwork.entities.User;
import com.intern.fwork.entities.Workspace;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class KanbanEndToEndIntegrationTest {

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

        // OWNER
        ownerUser = User.builder()
                .name("Owner")
                .email("owner@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        ownerUser = userRepository.save(ownerUser);

        // ADMIN
        adminUser = User.builder()
                .name("Admin")
                .email("admin@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        adminUser = userRepository.save(adminUser);

        // MEMBER
        memberUser = User.builder()
                .name("Member")
                .email("member@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        memberUser = userRepository.save(memberUser);
    }

    private void authenticate(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        currentAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(currentAuth);
    }

    @Test
    public void testCompleteKanbanE2EStory() throws Exception {
        // 1. Owner authenticates
        authenticate(ownerUser);

        // 2. Create Workspace
        CreateWorkspaceRequest createWorkspaceReq = new CreateWorkspaceRequest();
        createWorkspaceReq.setName("Kanban Project");
        createWorkspaceReq.setDescription("Project workspace");

        String wsRes = mockMvc.perform(post("/api/workspaces")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createWorkspaceReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID workspaceId = UUID.fromString(objectMapper.readTree(wsRes).path("data").path("id").asText());

        // 3. Add ADMIN member
        AddMemberRequest addAdminReq = new AddMemberRequest();
        addAdminReq.setEmail(adminUser.getEmail());
        addAdminReq.setRole(WorkspaceRole.ADMIN);

        mockMvc.perform(post("/api/workspaces/" + workspaceId + "/members")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addAdminReq)))
                .andExpect(status().isCreated());

        // 4. Add MEMBER member
        AddMemberRequest addMemberReq = new AddMemberRequest();
        addMemberReq.setEmail(memberUser.getEmail());
        addMemberReq.setRole(WorkspaceRole.MEMBER);

        mockMvc.perform(post("/api/workspaces/" + workspaceId + "/members")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addMemberReq)))
                .andExpect(status().isCreated());

        // 5. Create Board
        CreateBoardRequest createBoardReq = new CreateBoardRequest();
        createBoardReq.setTitle("Sprint Board");
        createBoardReq.setDescription("Main agile board");
        createBoardReq.setWorkspaceId(workspaceId);

        String boardRes = mockMvc.perform(post("/api/boards")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBoardReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID boardId = UUID.fromString(objectMapper.readTree(boardRes).path("data").path("id").asText());

        // 6. Create BoardColumns (TODO, IN PROGRESS, Review, Done)
        // ADMIN creates columns
        authenticate(adminUser);

        CreateBoardColumnRequest colReq = new CreateBoardColumnRequest();
        colReq.setName("TODO");
        colReq.setPosition(0);

        String col1Res = mockMvc.perform(post("/api/boards/" + boardId + "/columns")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(colReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID todoColId = UUID.fromString(objectMapper.readTree(col1Res).path("data").path("id").asText());

        colReq.setName("IN PROGRESS");
        colReq.setPosition(1);

        String col2Res = mockMvc.perform(post("/api/boards/" + boardId + "/columns")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(colReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID inProgressColId = UUID.fromString(objectMapper.readTree(col2Res).path("data").path("id").asText());

        // 7. Create Tasks
        // MEMBER creates task in TODO column
        authenticate(memberUser);

        CreateTaskRequest taskReq = new CreateTaskRequest();
        taskReq.setTitle("Implement JWT Filter");
        taskReq.setDescription("Secure REST APIs with JWT");
        taskReq.setPriority(Priority.HIGH);
        taskReq.setPosition(0);

        String task1Res = mockMvc.perform(post("/api/columns/" + todoColId + "/tasks")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.createdBy", is(memberUser.getId().toString())))
                .andReturn().getResponse().getContentAsString();
        UUID task1Id = UUID.fromString(objectMapper.readTree(task1Res).path("data").path("id").asText());

        taskReq.setTitle("Fix JDBC connection pool");
        taskReq.setDescription("Tune parameters");
        taskReq.setPosition(1);

        String task2Res = mockMvc.perform(post("/api/columns/" + todoColId + "/tasks")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID task2Id = UUID.fromString(objectMapper.readTree(task2Res).path("data").path("id").asText());

        // 8. Move Tasks (Kanban flow)
        // Move "Implement JWT Filter" (task1) to "IN PROGRESS" (col2) at position 0
        MoveTaskRequest moveReq = new MoveTaskRequest();
        moveReq.setTargetColumnId(inProgressColId);
        moveReq.setTargetPosition(0);

        mockMvc.perform(patch("/api/tasks/" + task1Id + "/move")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveReq)))
                .andExpect(status().isOk());

        // Verify TODO has only "Fix JDBC connection pool" (reindexed to position 0)
        mockMvc.perform(get("/api/columns/" + todoColId + "/tasks")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(task2Id.toString())))
                .andExpect(jsonPath("$.data[0].position", is(0)));

        // Verify IN PROGRESS has "Implement JWT Filter" (position 0)
        mockMvc.perform(get("/api/columns/" + inProgressColId + "/tasks")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(task1Id.toString())))
                .andExpect(jsonPath("$.data[0].position", is(0)));

        // 9. Update Task
        UpdateTaskRequest updateReq = new UpdateTaskRequest();
        updateReq.setTitle("Implement JWT Filter V2");
        updateReq.setDescription("Use Spring Security Entrypoint");
        updateReq.setPriority(Priority.LOW);

        mockMvc.perform(put("/api/tasks/" + task1Id)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title", is("Implement JWT Filter V2")))
                .andExpect(jsonPath("$.data.updatedBy", is(memberUser.getId().toString())));

        // 10. Delete Task (Archive)
        // MEMBER tries to delete -> Expected 403 Forbidden
        mockMvc.perform(delete("/api/tasks/" + task1Id)
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());

        // OWNER deletes task -> Expected 200 OK
        authenticate(ownerUser);
        mockMvc.perform(delete("/api/tasks/" + task1Id)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());

        // Verify deleted task is gone from GET responses
        mockMvc.perform(get("/api/columns/" + inProgressColId + "/tasks")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
