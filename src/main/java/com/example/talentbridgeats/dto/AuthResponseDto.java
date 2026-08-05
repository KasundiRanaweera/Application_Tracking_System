package com.example.talentbridgeats.dto;

import com.example.talentbridgeats.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDto {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private Role role;
}

