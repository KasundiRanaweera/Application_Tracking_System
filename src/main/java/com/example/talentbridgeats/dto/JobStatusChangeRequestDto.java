package com.example.talentbridgeats.dto;

import com.example.talentbridgeats.model.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusChangeRequestDto {

    @NotNull(message = "Status is required")
    private JobStatus status;
}
