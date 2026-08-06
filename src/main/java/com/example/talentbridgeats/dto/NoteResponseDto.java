package com.example.talentbridgeats.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NoteResponseDto {
    private Long id;
    private String content;
    private String recruiterName;
    private LocalDateTime createdAt;
}