package com.example.talentbridgeats.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyRequestDto {

    @NotNull(message = "Job ID is required")
    private Long jobId;

    private String resumeUrl;

    private String coverNote;
}