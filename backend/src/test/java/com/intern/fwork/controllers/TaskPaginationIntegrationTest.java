package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.CreateCommentRequest;
import com.intern.fwork.entities.*;
import com.intern.fwork.enums.Role;
import com.intern.fwork.enums.TaskActivityAction;
import com.intern.fwork.enums.WorkspaceRole;
import com.intern.fwork.repositories.*;
import com.intern.fwork.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TaskPaginationIntegrationTest {

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
    private CommentRepository commentRepository;

    @Autowired
    private TaskActivityRepository taskActivityRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Authentication currentAuth;

    private User ownerUser;
    private Workspace workspace;
    private Board board;
    private BoardColumn column;
    private Task task;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        commentRepository.deleteAll();
        taskActivityRepository.deleteAll();
        taskRepository.deleteAll();
        boardColumnRepository.deleteAll();
        boardRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        workspaceRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        ownerUser = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@fwork.com")
                .passwordHash("pwd")
                .role(Role.USER)
                .build());

        workspace = workspaceRepository.save(Workspace.builder()
                .name("WS")
                .slug("ws")
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .updatedBy(ownerUser)
                .build());

        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace).user(ownerUser).role(WorkspaceRole.OWNER).build());

        board = boardRepository.save(Board.builder()
                .title("Board")
                .workspace(workspace)
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .build());

        column = boardColumnRepository.save(BoardColumn.builder()
                .name("Col")
                .position(0)
                .board(board)
                .build());

        task = taskRepository.save(Task.builder()
                .title("Task A")
                .position(0)
                .column(column)
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .build());
    }

    private void authenticate(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        currentAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(currentAuth);
    }

    @Test
    public void testCommentsPagination() throws Exception {
        authenticate(ownerUser);

        // Add 5 comments
        for (int i = 1; i <= 5; i++) {
            CreateCommentRequest request = new CreateCommentRequest();
            request.setContent("Comment " + i);
            mockMvc.perform(post("/api/tasks/" + task.getId() + "/comments")
                            .with(authentication(currentAuth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Retrieve comments paginated: page=0, size=2
        mockMvc.perform(get("/api/tasks/" + task.getId() + "/comments")
                        .with(authentication(currentAuth))
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements", is(5)))
                .andExpect(jsonPath("$.data.totalPages", is(3)));
    }

    @Test
    public void testTaskActivityPagination() throws Exception {
        authenticate(ownerUser);

        // Log 5 activities manually in repository
        for (int i = 1; i <= 5; i++) {
            taskActivityRepository.save(TaskActivity.builder()
                    .task(task)
                    .actor(ownerUser)
                    .action(TaskActivityAction.TASK_CREATED)
                    .detail("Action " + i)
                    .build());
        }

        // Retrieve activities paginated: page=0, size=2
        mockMvc.perform(get("/api/tasks/" + task.getId() + "/activity")
                        .with(authentication(currentAuth))
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements", is(5)))
                .andExpect(jsonPath("$.data.totalPages", is(3)));
    }
}
