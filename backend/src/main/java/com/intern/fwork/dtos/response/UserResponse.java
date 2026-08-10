package com.intern.fwork.dtos.response;

import com.intern.fwork.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String avatar;
    private Role role;
}
