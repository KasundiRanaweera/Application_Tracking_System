package com.example.talentbridgeats.dto;

import com.example.talentbridgeats.model.ApplicationStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ApplicationDetailResponseDto {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String resumeUrl;
    private String coverNote;
    private ApplicationStatus status;
    private Integer rating;
    private List<NoteResponseDto> notes;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}