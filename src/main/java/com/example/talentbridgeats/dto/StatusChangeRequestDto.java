package com.example.talentbridgeats.dto;

import com.example.talentbridgeats.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusChangeRequestDto {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;
}