package com.example.talentbridgeats.dto;

import com.example.talentbridgeats.model.EmploymentType;
import com.example.talentbridgeats.model.WorkMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public class JobCreateRequestDto {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String location;

    private WorkMode workMode;

    private EmploymentType employmentType;

    @DecimalMin("0.0")
    private BigDecimal salaryMin;

    @DecimalMin("0.0")
    private BigDecimal salaryMax;

    private String requiredSkills;

    @FutureOrPresent(message = "Closing date must be in the future")
    private LocalDate closingDate;
}
