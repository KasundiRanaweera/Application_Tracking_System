package com.example.talentbridgeats.service;

import com.example.talentbridgeats.dto.*;
import com.example.talentbridgeats.exception.*;
import com.example.talentbridgeats.model.*;
import com.example.talentbridgeats.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationNoteRepository applicationNoteRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    // Candidate: Apply to a job
    public ApplicationSummaryResponseDto apply(ApplyRequestDto request, Long candidateId) {
        // Check job exists and is OPEN
        Job job = jobRepository.findOpenJobById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found or not open"));

        // Check candidate exists
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));

        // Check for duplicate application (FR-U6)
        if (applicationRepository.existsByJobIdAndCandidateId(job.getId(), candidateId)) {
            throw new DuplicateApplicationException("You have already applied to this job");
        }

        // Create application
        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .resumeUrl(request.getResumeUrl())
                .coverNote(request.getCoverNote())
                .status(ApplicationStatus.APPLIED)
                .build();

        Application saved = applicationRepository.save(application);
        return mapToSummaryResponse(saved);
    }

    // Candidate: Get own applications
    public Page<ApplicationSummaryResponseDto> getMyApplications(Long candidateId, Pageable pageable) {
        return applicationRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("candidate").get("id"), candidateId),
                pageable
        ).map(this::mapToSummaryResponse);
    }

    // Candidate: Get single own application (owner check)
    public ApplicationSummaryResponseDto getMyApplication(Long applicationId, Long candidateId) {
        Application application = applicationRepository
                .findByIdAndCandidateId(applicationId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        return mapToSummaryResponse(application);
    }

    // Candidate: Withdraw application (owner check)
    public void withdrawApplication(Long applicationId, Long candidateId) {
        Application application = applicationRepository
                .findByIdAndCandidateId(applicationId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        // Check if already in a terminal state
        if (application.getStatus() == ApplicationStatus.WITHDRAWN ||
                application.getStatus() == ApplicationStatus.REJECTED ||
                application.getStatus() == ApplicationStatus.HIRED) {
            throw new IllegalStateException("Cannot withdraw application in " + application.getStatus() + " status");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    // Recruiter: Get all applications for a job (with filtering/sorting)
    public Page<ApplicationDetailResponseDto> getApplicationsByJob(Long jobId, Specification<Application> spec, Pageable pageable) {
        Specification<Application> byJob = (root, query, cb) ->
                cb.equal(root.get("job").get("id"), jobId);
        Specification<Application> combined = spec == null ? byJob : byJob.and(spec);

        return applicationRepository.findAll(combined, pageable)
                .map(this::mapToDetailResponse);
    }

    // Recruiter: Get single application detail
    public ApplicationDetailResponseDto getApplicationDetail(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        return mapToDetailResponse(application);
    }

    // Recruiter: Rate application
    public ApplicationDetailResponseDto rateApplication(Long applicationId, RatingRequestDto request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        application.setRating(request.getRating());
        Application updated = applicationRepository.save(application);
        return mapToDetailResponse(updated);
    }

    // Recruiter: Add note to application
    public NoteResponseDto addNote(Long applicationId, NoteRequestDto request, Long recruiterId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));

        ApplicationNote note = ApplicationNote.builder()
                .application(application)
                .recruiter(recruiter)
                .content(request.getContent())
                .build();

        ApplicationNote saved = applicationNoteRepository.save(note);
        return mapNoteToResponse(saved);
    }

    // Recruiter: Change application status (with validation)
    public ApplicationDetailResponseDto changeApplicationStatus(Long applicationId, StatusChangeRequestDto request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        ApplicationStatus newStatus = request.getStatus();
        ApplicationStatus currentStatus = application.getStatus();

        // Validate transition
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new IllegalStateException("Invalid status transition: " + currentStatus + " → " + newStatus);
        }

        application.setStatus(newStatus);
        Application updated = applicationRepository.save(application);
        return mapToDetailResponse(updated);
    }

    // Helper: Validate status transition
    private boolean isValidStatusTransition(ApplicationStatus current, ApplicationStatus next) {
        return switch (current) {
            case APPLIED -> next == ApplicationStatus.UNDER_REVIEW || next == ApplicationStatus.REJECTED || next == ApplicationStatus.WITHDRAWN;
            case UNDER_REVIEW -> next == ApplicationStatus.SHORTLISTED || next == ApplicationStatus.REJECTED || next == ApplicationStatus.WITHDRAWN;
            case SHORTLISTED -> next == ApplicationStatus.INTERVIEW || next == ApplicationStatus.REJECTED || next == ApplicationStatus.WITHDRAWN;
            case INTERVIEW -> next == ApplicationStatus.OFFER || next == ApplicationStatus.REJECTED || next == ApplicationStatus.WITHDRAWN;
            case OFFER -> next == ApplicationStatus.HIRED || next == ApplicationStatus.REJECTED || next == ApplicationStatus.WITHDRAWN;
            case HIRED, REJECTED, WITHDRAWN -> false; // No transitions from terminal states
        };
    }

    // Helper: Map to summary response (candidate view - no rating/notes)
    private ApplicationSummaryResponseDto mapToSummaryResponse(Application app) {
        return ApplicationSummaryResponseDto.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .companyName("TalentBridge") // single company
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }

    // Helper: Map to detail response (recruiter view - includes rating/notes)
    private ApplicationDetailResponseDto mapToDetailResponse(Application app) {
        List<NoteResponseDto> notes = applicationNoteRepository.findByApplicationId(app.getId())
                .stream()
                .map(this::mapNoteToResponse)
                .collect(Collectors.toList());

        return ApplicationDetailResponseDto.builder()
                .id(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .candidateId(app.getCandidate().getId())
                .candidateName(app.getCandidate().getName())
                .candidateEmail(app.getCandidate().getEmail())
                .resumeUrl(app.getResumeUrl())
                .coverNote(app.getCoverNote())
                .status(app.getStatus())
                .rating(app.getRating())
                .notes(notes)
                .appliedAt(app.getAppliedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }

    // Helper: Map note to response
    private NoteResponseDto mapNoteToResponse(ApplicationNote note) {
        return NoteResponseDto.builder()
                .id(note.getId())
                .content(note.getContent())
                .recruiterName(note.getRecruiter().getName())
                .createdAt(note.getCreatedAt())
                .build();
    }
}