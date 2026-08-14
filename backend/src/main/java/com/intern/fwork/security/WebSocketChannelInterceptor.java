package com.intern.fwork.security;

import com.intern.fwork.repositories.WorkspaceMemberRepository;
import com.intern.fwork.repositories.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Intercepts WebSocket STOMP frames to:
 * - CONNECT: extract and validate JWT from Authorization header, set security principal
 * - SUBSCRIBE: enforce workspace membership when subscribing to /topic/boards/{boardId}
 */
@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final BoardRepository boardRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();

        // ── CONNECT: authenticate via JWT ────────────────────────────────────
        if (StompCommand.CONNECT.equals(command)) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "WebSocket CONNECT requires Authorization: Bearer <token>"
                );
            }

            String jwt = authHeader.substring(7);
            try {
                String username = jwtService.extractUsername(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    throw new org.springframework.security.access.AccessDeniedException("Invalid or expired JWT token");
                }
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                accessor.setUser(authentication);
            } catch (org.springframework.security.access.AccessDeniedException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new org.springframework.security.access.AccessDeniedException("JWT validation failed: " + ex.getMessage());
            }
        }

        // ── SUBSCRIBE: enforce workspace access for /topic/boards/{boardId} ──
        if (StompCommand.SUBSCRIBE.equals(command)) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/topic/boards/")) {
                String boardIdStr = destination.substring("/topic/boards/".length());
                // Strip any further path segments
                int slashIndex = boardIdStr.indexOf('/');
                if (slashIndex > 0) boardIdStr = boardIdStr.substring(0, slashIndex);

                try {
                    UUID boardId = UUID.fromString(boardIdStr);
                    CustomUserDetails userDetails = resolveUserDetails(accessor);
                    if (userDetails == null) {
                        throw new org.springframework.security.access.AccessDeniedException("Not authenticated");
                    }

                    // Resolve workspaceId for this board, then check membership
                    boardRepository.findById(boardId).ifPresentOrElse(
                            board -> {
                                UUID workspaceId = board.getWorkspace().getId();
                                UUID userId = userDetails.getUser().getId();
                                boolean isMember = workspaceMemberRepository
                                        .findByWorkspaceIdAndUserId(workspaceId, userId)
                                        .isPresent();
                                if (!isMember) {
                                    throw new org.springframework.security.access.AccessDeniedException(
                                            "Access denied to board " + boardId
                                    );
                                }
                            },
                            () -> {
                                throw new org.springframework.security.access.AccessDeniedException(
                                        "Board not found: " + boardId
                                );
                            }
                    );
                } catch (IllegalArgumentException ex) {
                    throw new org.springframework.security.access.AccessDeniedException("Invalid board ID in subscription path");
                }
            }
        }

        return message;
    }

    private CustomUserDetails resolveUserDetails(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details;
        }
        return null;
    }
}
