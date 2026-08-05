package com.example.talentbridgeats.dto;

import com.example.talentbridgeats.model.EmploymentType;
import com.example.talentbridgeats.model.JobStatus;
import com.example.talentbridgeats.model.WorkMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class JobResponseDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private WorkMode workMode;
    private EmploymentType employmentType;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String requiredSkills;
    private JobStatus status;
    private LocalDate closingDate;
    private Long postedById;
    private String postedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
