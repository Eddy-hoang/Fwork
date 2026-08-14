package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.AssignTaskRequest;
import com.intern.fwork.dtos.request.CreateCommentRequest;
import com.intern.fwork.entities.*;
import com.intern.fwork.enums.Role;
import com.intern.fwork.enums.WorkspaceRole;
import com.intern.fwork.repositories.*;
import com.intern.fwork.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
@ActiveProfiles("test")
public class NotificationIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Authentication currentAuth;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

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
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TaskActivityRepository taskActivityRepository;

    private User actorUser;
    private User recipientUser;
    private Workspace workspace;
    private Board board;
    private BoardColumn column;
    private Task task;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        cleanup();

        // 1. Create Actor
        actorUser = User.builder()
                .name("Actor User")
                .email("actor_notify@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        actorUser = userRepository.save(actorUser);

        // 2. Create Recipient
        recipientUser = User.builder()
                .name("Recipient User")
                .email("recipient_notify@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build();
        recipientUser = userRepository.save(recipientUser);

        // 3. Create Workspace & Members
        workspace = Workspace.builder()
                .name("Notification Workspace")
                .slug("notify-workspace")
                .description("Desc")
                .createdBy(actorUser)
                .updatedBy(actorUser)
                .build();
        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member1 = WorkspaceMember.builder()
                .workspace(workspace)
                .user(actorUser)
                .role(WorkspaceRole.OWNER)
                .build();
        workspaceMemberRepository.save(member1);

        WorkspaceMember member2 = WorkspaceMember.builder()
                .workspace(workspace)
                .user(recipientUser)
                .role(WorkspaceRole.MEMBER)
                .build();
        workspaceMemberRepository.save(member2);

        // 4. Create Board
        board = Board.builder()
                .title("Notify Board")
                .description("Desc")
                .color("#FFFFFF")
                .workspace(workspace)
                .createdBy(actorUser)
                .updatedBy(actorUser)
                .isArchived(false)
                .position(0)
                .build();
        board = boardRepository.save(board);

        // 5. Create Column
        column = BoardColumn.builder()
                .name("To Do")
                .position(0)
                .board(board)
                .build();
        column = boardColumnRepository.save(column);

        // 6. Create Task
        task = Task.builder()
                .title("Notify Task")
                .description("Desc")
                .column(column)
                .createdBy(actorUser)
                .updatedBy(actorUser)
                .isArchived(false)
                .position(0)
                .build();
        task = taskRepository.save(task);

        // Set default security context to Actor
        authenticateAs(actorUser);
    }

    @AfterEach
    public void teardown() {
        cleanup();
    }

    private void cleanup() {
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        taskActivityRepository.deleteAll();
        taskRepository.deleteAll();
        boardColumnRepository.deleteAll();
        boardRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void authenticateAs(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        currentAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(currentAuth);
    }

    @Test
    public void testNotificationEndToEndFlow() throws Exception {
        UUID taskId = task.getId();

        // Step 1: Assign Task to recipientUser (authenticated as actorUser)
        AssignTaskRequest assignRequest = new AssignTaskRequest();
        assignRequest.setAssigneeId(recipientUser.getId());

        mockMvc.perform(patch("/api/tasks/" + taskId + "/assignee")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk());

        // Wait until notification is committed to the database
        boolean written1 = false;
        for (int i = 0; i < 50; i++) {
            if (notificationRepository.count() == 1) {
                written1 = true;
                break;
            }
            Thread.sleep(100);
        }
        org.junit.jupiter.api.Assertions.assertTrue(written1, "Notification was not written in time");

        // Step 2: Query notifications for recipientUser
        authenticateAs(recipientUser);

        // Verify unread count is 1
        mockMvc.perform(get("/api/notifications/unread-count")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));

        // Get notifications list
        mockMvc.perform(get("/api/notifications")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].type").value("TASK_ASSIGNED"))
                .andExpect(jsonPath("$.data.content[0].read").value(false))
                .andExpect(jsonPath("$.data.content[0].actor.name").value("Actor User"));

        // Retrieve notification ID
        List<Notification> notifications = notificationRepository.findAll();
        UUID notificationId = notifications.get(0).getId();

        // Step 3: Add comment by actorUser
        authenticateAs(actorUser);

        CreateCommentRequest commentRequest = new CreateCommentRequest();
        commentRequest.setContent("This is a notification comment");

        mockMvc.perform(post("/api/tasks/" + taskId + "/comments")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated());

        // Wait until second notification is committed
        boolean written2 = false;
        for (int i = 0; i < 50; i++) {
            if (notificationRepository.count() == 2) {
                written2 = true;
                break;
            }
            Thread.sleep(100);
        }
        org.junit.jupiter.api.Assertions.assertTrue(written2, "Second notification was not written in time");

        // Step 4: Verify recipientUser has 2 notifications now
        authenticateAs(recipientUser);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(2));

        // Mark the first notification as read
        mockMvc.perform(patch("/api/notifications/" + notificationId + "/read")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());

        // Verify unread count decreased to 1
        mockMvc.perform(get("/api/notifications/unread-count")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1));

        // Mark all as read
        mockMvc.perform(patch("/api/notifications/read-all")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());

        // Verify unread count is 0
        mockMvc.perform(get("/api/notifications/unread-count")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0));
    }
}
