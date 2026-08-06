package com.example.talentbridgeats.dto;

import com.example.talentbridgeats.model.EmploymentType;
import com.example.talentbridgeats.model.WorkMode;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobUpdateRequestDto {

    private String title;

    private String description;

    private String location;

    private WorkMode workMode;

    private EmploymentType employmentType;

    @DecimalMin("0.0")
    private BigDecimal salaryMin;

    @DecimalMin("0.0")
    private BigDecimal salaryMax;

    private String requiredSkills;

    private LocalDate closingDate;
}
