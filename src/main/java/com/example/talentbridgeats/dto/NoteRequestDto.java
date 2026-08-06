package com.example.talentbridgeats.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteRequestDto {

    @NotBlank(message = "Note content is required")
    private String content;
}