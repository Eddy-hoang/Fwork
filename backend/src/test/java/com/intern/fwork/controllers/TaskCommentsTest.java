package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.CreateCommentRequest;
import com.intern.fwork.dtos.request.UpdateCommentRequest;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class TaskCommentsTest {

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
    private RefreshTokenRepository refreshTokenRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Authentication currentAuth;

    private User ownerUser;
    private User adminUser;
    private User memberUser;
    private User outsiderUser;
    private Workspace workspace;
    private Task taskA;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        commentRepository.deleteAll();
        taskRepository.deleteAll();
        boardColumnRepository.deleteAll();
        boardRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        workspaceRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        ownerUser = userRepository.save(User.builder()
                .name("Owner User")
                .email("owner@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build());

        adminUser = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build());

        memberUser = userRepository.save(User.builder()
                .name("Member User")
                .email("member@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build());

        outsiderUser = userRepository.save(User.builder()
                .name("Outsider User")
                .email("outsider@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build());

        workspace = workspaceRepository.save(Workspace.builder()
                .name("Workspace")
                .slug("test-workspace-comments")
                .description("Test workspace")
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .build());

        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace).user(ownerUser).role(WorkspaceRole.OWNER).build());
        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace).user(adminUser).role(WorkspaceRole.ADMIN).build());
        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace).user(memberUser).role(WorkspaceRole.MEMBER).build());

        Board board = boardRepository.save(Board.builder()
                .title("Board A")
                .workspace(workspace)
                .createdBy(ownerUser)
                .updatedBy(ownerUser)
                .build());

        BoardColumn col = boardColumnRepository.save(BoardColumn.builder()
                .name("TODO")
                .position(0)
                .board(board)
                .build());

        taskA = taskRepository.save(Task.builder()
                .title("Task A")
                .position(0)
                .column(col)
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

    // Helper: create a comment and return its id
    private UUID createCommentAs(User user, String content) throws Exception {
        authenticate(user);
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent(content);

        MvcResult result = mockMvc.perform(post("/api/tasks/" + taskA.getId() + "/comments")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(node.path("data").path("id").asText());
    }

    @Test
    public void testCommentsAccessControl() throws Exception {
        // 1. Outsider cannot create comment -> 403
        authenticate(outsiderUser);
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("Outsider comment");
        mockMvc.perform(post("/api/tasks/" + taskA.getId() + "/comments")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        // 2. Outsider cannot list comments -> 403
        mockMvc.perform(get("/api/tasks/" + taskA.getId() + "/comments")
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());

        // 3. MEMBER can create comment
        UUID commentId = createCommentAs(memberUser, "First comment by member");

        // 4. MEMBER can list comments
        authenticate(memberUser);
        mockMvc.perform(get("/api/tasks/" + taskA.getId() + "/comments")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].content", is("First comment by member")))
                .andExpect(jsonPath("$.data[0].createdBy.email", is("member@fwork.com")));

        // 5. OWNER can also create a comment
        UUID ownerCommentId = createCommentAs(ownerUser, "Owner comment");

        // 6. Both comments are listed in order
        authenticate(memberUser);
        mockMvc.perform(get("/api/tasks/" + taskA.getId() + "/comments")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    public void testCommentEditControl() throws Exception {
        // Member creates a comment
        UUID commentId = createCommentAs(memberUser, "Original content");

        // Author can edit own comment
        authenticate(memberUser);
        UpdateCommentRequest updateReq = new UpdateCommentRequest();
        updateReq.setContent("Updated content");
        mockMvc.perform(put("/api/comments/" + commentId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", is("Updated content")));

        // ADMIN (non-author) cannot edit the comment -> 403
        authenticate(adminUser);
        mockMvc.perform(put("/api/comments/" + commentId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());

        // OWNER (non-author) cannot edit the comment -> 403
        authenticate(ownerUser);
        mockMvc.perform(put("/api/comments/" + commentId)
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCommentDeleteControl() throws Exception {
        // Member creates a comment
        UUID memberCommentId = createCommentAs(memberUser, "Member's comment");

        // Another MEMBER (ownerUser in MEMBER role is not applicable here; use adminUser as non-author)
        // We create another member-level user for isolation test
        User anotherMember = userRepository.save(User.builder()
                .name("Another Member")
                .email("another@fwork.com")
                .passwordHash("password")
                .role(Role.USER)
                .build());
        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace).user(anotherMember).role(WorkspaceRole.MEMBER).build());

        // anotherMember cannot delete memberUser's comment -> 403
        authenticate(anotherMember);
        mockMvc.perform(delete("/api/comments/" + memberCommentId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());

        // ADMIN can delete any comment
        authenticate(adminUser);
        mockMvc.perform(delete("/api/comments/" + memberCommentId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());

        // Verify comment is gone
        authenticate(memberUser);
        mockMvc.perform(get("/api/tasks/" + taskA.getId() + "/comments")
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));

        // Author can delete own comment
        UUID ownCommentId = createCommentAs(memberUser, "My own comment");
        authenticate(memberUser);
        mockMvc.perform(delete("/api/comments/" + ownCommentId)
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());
    }

    @Test
    public void testCommentValidation() throws Exception {
        authenticate(memberUser);

        // Blank content -> 400
        CreateCommentRequest blankReq = new CreateCommentRequest();
        blankReq.setContent("  ");
        mockMvc.perform(post("/api/tasks/" + taskA.getId() + "/comments")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankReq)))
                .andExpect(status().isBadRequest());

        // Too long content -> 400
        CreateCommentRequest longReq = new CreateCommentRequest();
        longReq.setContent("x".repeat(2001));
        mockMvc.perform(post("/api/tasks/" + taskA.getId() + "/comments")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUnauthenticatedRequestDenied() throws Exception {
        // No authentication header -> 401
        mockMvc.perform(get("/api/tasks/" + taskA.getId() + "/comments"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/tasks/" + taskA.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"test\"}"))
                .andExpect(status().isUnauthorized());
    }
}
