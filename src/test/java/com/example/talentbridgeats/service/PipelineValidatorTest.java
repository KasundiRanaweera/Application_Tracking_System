package com.example.talentbridgeats.service;

import com.example.talentbridgeats.exception.AccessDeniedException;
import com.example.talentbridgeats.model.ApplicationStatus;
import com.example.talentbridgeats.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PipelineValidatorTest {

    private PipelineValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PipelineValidator();
    }

    // ==================== LEGAL FORWARD TRANSITIONS (RECRUITER) ====================

    @Test
    void recruiter_can_move_APPLIED_to_UNDER_REVIEW() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.APPLIED, ApplicationStatus.UNDER_REVIEW, Role.RECRUITER));
    }

    @Test
    void recruiter_can_move_UNDER_REVIEW_to_SHORTLISTED() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.SHORTLISTED, Role.RECRUITER));
    }

    @Test
    void recruiter_can_move_SHORTLISTED_to_INTERVIEW() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.SHORTLISTED, ApplicationStatus.INTERVIEW, Role.RECRUITER));
    }

    @Test
    void recruiter_can_move_INTERVIEW_to_OFFER() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.INTERVIEW, ApplicationStatus.OFFER, Role.RECRUITER));
    }

    @Test
    void recruiter_can_move_OFFER_to_HIRED() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.OFFER, ApplicationStatus.HIRED, Role.RECRUITER));
    }

    // ==================== LEGAL REJECT TRANSITIONS (RECRUITER) ====================

    @Test
    void recruiter_can_reject_from_APPLIED() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.APPLIED, ApplicationStatus.REJECTED, Role.RECRUITER));
    }

    @Test
    void recruiter_can_reject_from_UNDER_REVIEW() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.REJECTED, Role.RECRUITER));
    }

    @Test
    void recruiter_can_reject_from_SHORTLISTED() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED, Role.RECRUITER));
    }

    @Test
    void recruiter_can_reject_from_INTERVIEW() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED, Role.RECRUITER));
    }

    @Test
    void recruiter_can_reject_from_OFFER() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.OFFER, ApplicationStatus.REJECTED, Role.RECRUITER));
    }

    // ==================== LEGAL WITHDRAW TRANSITIONS (CANDIDATE) ====================

    @Test
    void candidate_can_withdraw_from_APPLIED() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.APPLIED, ApplicationStatus.WITHDRAWN, Role.USER));
    }

    @Test
    void candidate_can_withdraw_from_UNDER_REVIEW() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.WITHDRAWN, Role.USER));
    }

    @Test
    void candidate_can_withdraw_from_SHORTLISTED() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.SHORTLISTED, ApplicationStatus.WITHDRAWN, Role.USER));
    }

    @Test
    void candidate_can_withdraw_from_INTERVIEW() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.INTERVIEW, ApplicationStatus.WITHDRAWN, Role.USER));
    }

    @Test
    void candidate_can_withdraw_from_OFFER() {
        assertDoesNotThrow(() ->
                validator.validate(ApplicationStatus.OFFER, ApplicationStatus.WITHDRAWN, Role.USER));
    }

    // ==================== ILLEGAL SKIPS ====================

    @Test
    void recruiter_cannot_skip_APPLIED_to_SHORTLISTED() {
        assertThrows(IllegalStateException.class, () ->
                validator.validate(ApplicationStatus.APPLIED, ApplicationStatus.SHORTLISTED, Role.RECRUITER));
    }

    @Test
    void recruiter_cannot_skip_APPLIED_to_INTERVIEW() {
        assertThrows(IllegalStateException.class, () ->
                validator.validate(ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEW, Role.RECRUITER));
    }

    @Test
    void recruiter_cannot_skip_APPLIED_to_HIRED() {
        assertThrows(IllegalStateException.class, () ->
                validator.validate(ApplicationStatus.APPLIED, ApplicationStatus.HIRED, Role.RECRUITER));
    }

    @Test
    void recruiter_cannot_go_backward_INTERVIEW_to_APPLIED() {
        assertThrows(IllegalStateException.class, () ->
                validator.validate(ApplicationStatus.INTERVIEW, ApplicationStatus.APPLIED, Role.RECRUITER));
    }

    @Test
    void recruiter_cannot_go_backward_SHORTLISTED_to_UNDER_REVIEW() {
        assertThrows(IllegalStateException.class, () ->
                validator.validate(ApplicationStatus.SHORTLISTED, ApplicationStatus.UNDER_REVIEW, Role.RECRUITER));
    }

    // ==================== TERMINAL STATES ====================

    @Test
    void no_transition_from_HIRED() {
        assertThrows(IllegalStateException.class, () ->
                validator.validate(ApplicationStatus.HIRED, ApplicationStatus.INTERVIEW, Role.RECRUITER));
    }

    @Test
    void no_transition_from_REJECTED() {
        assertThrows(IllegalStateException.class, () ->
                validator.validate(ApplicationStatus.REJECTED, ApplicationStatus.APPLIED, Role.RECRUITER));
    }

    @Test
    void no_transition_from_WITHDRAWN() {
        assertThrows(IllegalStateException.class, () ->
                validator.validate(ApplicationStatus.WITHDRAWN, ApplicationStatus.APPLIED, Role.USER));
    }

    // ==================== ROLE VIOLATIONS ====================

    @Test
    void recruiter_cannot_withdraw_application() {
        assertThrows(AccessDeniedException.class, () ->
                validator.validate(ApplicationStatus.APPLIED, ApplicationStatus.WITHDRAWN, Role.RECRUITER));
    }

    @Test
    void candidate_cannot_advance_status() {
        assertThrows(AccessDeniedException.class, () ->
                validator.validate(ApplicationStatus.APPLIED, ApplicationStatus.UNDER_REVIEW, Role.USER));
    }

    @Test
    void candidate_cannot_reject_application() {
        assertThrows(AccessDeniedException.class, () ->
                validator.validate(ApplicationStatus.APPLIED, ApplicationStatus.REJECTED, Role.USER));
    }

    @Test
    void candidate_cannot_hire_themselves() {
        assertThrows(AccessDeniedException.class, () ->
                validator.validate(ApplicationStatus.OFFER, ApplicationStatus.HIRED, Role.USER));
    }
}