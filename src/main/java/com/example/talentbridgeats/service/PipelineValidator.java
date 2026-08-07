package com.example.talentbridgeats.service;

import com.example.talentbridgeats.exception.AccessDeniedException;
import com.example.talentbridgeats.model.ApplicationStatus;
import com.example.talentbridgeats.model.Role;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class PipelineValidator {

    // Defines every legal transition from each status
    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS =
            Map.of(
                    ApplicationStatus.APPLIED,      Set.of(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
                    ApplicationStatus.UNDER_REVIEW, Set.of(ApplicationStatus.SHORTLISTED,  ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
                    ApplicationStatus.SHORTLISTED,  Set.of(ApplicationStatus.INTERVIEW,    ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
                    ApplicationStatus.INTERVIEW,    Set.of(ApplicationStatus.OFFER,        ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
                    ApplicationStatus.OFFER,        Set.of(ApplicationStatus.HIRED,        ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
                    ApplicationStatus.HIRED,        Set.of(),
                    ApplicationStatus.REJECTED,     Set.of(),
                    ApplicationStatus.WITHDRAWN,    Set.of()
            );

    /**
     * Validates that the requested status transition is legal
     * and that the actor (role) is allowed to make that specific move.
     *
     * Rules:
     * - WITHDRAWN can only be set by a CANDIDATE (the owner)
     * - HIRED, REJECTED, UNDER_REVIEW, SHORTLISTED, INTERVIEW, OFFER can only be set by RECRUITER
     * - No transitions out of terminal states (HIRED, REJECTED, WITHDRAWN)
     */
    public void validate(ApplicationStatus current, ApplicationStatus requested, Role actorRole) {

        // 1. Check if the transition exists at all
        Set<ApplicationStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(requested)) {
            throw new IllegalStateException(
                    "Invalid status transition: " + current + " → " + requested
            );
        }

        // 2. WITHDRAWN can only be requested by CANDIDATE
        if (requested == ApplicationStatus.WITHDRAWN && actorRole != Role.USER) {
            throw new AccessDeniedException("Only candidates can withdraw an application");
        }

        // 3. All other moves (forward/reject) can only be made by RECRUITER
        if (requested != ApplicationStatus.WITHDRAWN && actorRole != Role.RECRUITER) {
            throw new AccessDeniedException("Only recruiters can change application status");
        }
    }
}