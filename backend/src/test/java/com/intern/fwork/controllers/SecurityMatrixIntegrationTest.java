package com.intern.fwork.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intern.fwork.dtos.request.CreateBoardRequest;
import com.intern.fwork.entities.*;
import com.intern.fwork.enums.Role;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SecurityMatrixIntegrationTest {

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Authentication currentAuth;

    private User ownerUser;
    private User adminUser;
    private User memberUser;
    private Workspace workspace;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

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

        adminUser = userRepository.save(User.builder()
                .name("Admin")
                .email("admin@fwork.com")
                .passwordHash("pwd")
                .role(Role.USER)
                .build());

        memberUser = userRepository.save(User.builder()
                .name("Member")
                .email("member@fwork.com")
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
        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace).user(adminUser).role(WorkspaceRole.ADMIN).build());
        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(workspace).user(memberUser).role(WorkspaceRole.MEMBER).build());
    }

    private void authenticate(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        currentAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(currentAuth);
    }

    @Test
    public void testBoardCreationPermissions() throws Exception {
        CreateBoardRequest request = new CreateBoardRequest();
        request.setTitle("New Board");
        request.setWorkspaceId(workspace.getId());
        request.setColor("#FFFFFF");

        // MEMBER cannot create board -> 403
        authenticate(memberUser);
        mockMvc.perform(post("/api/boards")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // ADMIN can create board -> 201
        authenticate(adminUser);
        mockMvc.perform(post("/api/boards")
                        .with(authentication(currentAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testWorkspaceMemberRemovalPermissions() throws Exception {
        // MEMBER cannot remove ADMIN -> 403
        authenticate(memberUser);
        mockMvc.perform(delete("/api/workspaces/" + workspace.getId() + "/members/" + adminUser.getId())
                        .with(authentication(currentAuth)))
                .andExpect(status().isForbidden());

        // ADMIN can remove MEMBER -> 200
        authenticate(adminUser);
        mockMvc.perform(delete("/api/workspaces/" + workspace.getId() + "/members/" + memberUser.getId())
                        .with(authentication(currentAuth)))
                .andExpect(status().isOk());
    }
}
