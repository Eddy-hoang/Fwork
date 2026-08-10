package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.AssignTaskRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class TaskAssignmentTest {

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
    private User adminUser;
    private User memberUser;
    private User outsiderUser;
    private Workspace workspace;
    private Task task;

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

        outsiderUser = User.builder()
                .name("Outsider User")
                .email("outsider@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        outsiderUser = userRepository.save(outsiderUser);

        // Create Workspace
        workspace = Workspace.builder()
                .name("Test Workspace")
                .slug("test-workspace")
                .description("Workspace for integration testing")
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
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
        Board board = Board.builder()
                .title("Board")
                .workspace(workspace)
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .build();
        board = boardRepository.save(board);

        // Create Column
        BoardColumn column = BoardColumn.builder()
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
    public void testTaskAssignmentMatrix() throws Exception {
        AssignTaskRequest request = new AssignTaskRequest();

        // 1. MEMBER assigns MEMBER -> Expected 200 OK
        authenticate(memberUser);
        request.setAssigneeId(memberUser.getId());
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/assignee")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.assigneeId", is(memberUser.getId().toString())));

        // 2. ADMIN assigns MEMBER -> Expected 200 OK
        authenticate(adminUser);
        request.setAssigneeId(memberUser.getId());
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/assignee")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 3. OWNER assigns MEMBER -> Expected 200 OK
        authenticate(ownerUser);
        request.setAssigneeId(memberUser.getId());
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/assignee")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 4. MEMBER assigns outsider -> Expected 403 Forbidden
        authenticate(memberUser);
        request.setAssigneeId(outsiderUser.getId());
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/assignee")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // 5. ADMIN assigns outsider -> Expected 403 Forbidden
        authenticate(adminUser);
        request.setAssigneeId(outsiderUser.getId());
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/assignee")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // 6. OWNER assigns outsider -> Expected 403 Forbidden
        authenticate(ownerUser);
        request.setAssigneeId(outsiderUser.getId());
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/assignee")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // 7. Outsider assigns anyone -> Expected 403 Forbidden
        authenticate(outsiderUser);
        request.setAssigneeId(memberUser.getId());
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/assignee")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // 8. Assign non-existent user ID -> Expected 404 Not Found
        authenticate(ownerUser);
        request.setAssigneeId(UUID.randomUUID());
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/assignee")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        // 9. Unassign -> Expected 200 OK and assigneeId is null
        authenticate(memberUser);
        request.setAssigneeId(null);
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/assignee")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.assigneeId", nullValue()));
    }
}
