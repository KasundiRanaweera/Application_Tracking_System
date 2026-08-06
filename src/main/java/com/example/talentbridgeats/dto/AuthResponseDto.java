package com.example.talentbridgeats.dto;

import com.example.talentbridgeats.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private Role role;
}

