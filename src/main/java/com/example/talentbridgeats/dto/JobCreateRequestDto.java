package com.example.talentbridgeats.dto;

import com.example.talentbridgeats.model.EmploymentType;
import com.example.talentbridgeats.model.WorkMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class JobCreateRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String location;
    private WorkMode workMode;
    private EmploymentType employmentType;

    @DecimalMin(value = "0.0", message = "Salary minimum must be positive")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0.0", message = "Salary maximum must be positive")
    private BigDecimal salaryMax;

    private String requiredSkills;
    private LocalDate closingDate;
}