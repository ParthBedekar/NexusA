package com.example.nexusa.Service;

import com.example.nexusa.Dto.GlobalDTOs.ResearchSubmissionCreateDTO;
import com.example.nexusa.Dto.GlobalDTOs.ResearchSubmissionResponseDTO;
import com.example.nexusa.Dto.GlobalDTOs.ResearchSubmissionUpdateDTO;
import com.example.nexusa.Model.Civilization;
import com.example.nexusa.Model.Enums.GlobalEnums.ResearchSubmissionStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.Role;
import com.example.nexusa.Model.ResearchSubmission;
import com.example.nexusa.Model.User;
import com.example.nexusa.Repository.GlobalRepositories.CivilizationRepository;
import com.example.nexusa.Repository.GlobalRepositories.ResearchSubmissionRepository;
import com.example.nexusa.Repository.GlobalRepositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@org.springframework.transaction.annotation.Transactional
public class ResearchSubmissionService {

    private final ResearchSubmissionRepository submissionRepository;
    private final CivilizationRepository civilizationRepository;
    private final UserRepository userRepository;

    public ResearchSubmissionService(ResearchSubmissionRepository submissionRepository,
                                     CivilizationRepository civilizationRepository,
                                     UserRepository userRepository) {
        this.submissionRepository = submissionRepository;
        this.civilizationRepository = civilizationRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        String mail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return userRepository.findUserByEmail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ResearchSubmissionResponseDTO createSubmission(ResearchSubmissionCreateDTO dto) {
        User user = getAuthenticatedUser();
        Civilization civ = civilizationRepository.findById(dto.getCivilizationId())
                .orElseThrow(() -> new RuntimeException("Civilization not found"));

        ResearchSubmission submission = new ResearchSubmission();
        submission.setResearcher(user);
        submission.setInstitution(user.getUniID());
        submission.setCivilizationReference(civ);
        submission.setSubmissionTitle(dto.getSubmissionTitle());
        submission.setSubmissionDescription(dto.getSubmissionDescription());
        submission.setRawStructuredPayload(dto.getRawStructuredPayload());
        submission.setSubmissionStatus(ResearchSubmissionStatus.DRAFT);

        ResearchSubmission saved = submissionRepository.save(submission);
        return mapToResponse(saved);
    }

    public ResearchSubmissionResponseDTO updateSubmission(UUID id, ResearchSubmissionUpdateDTO dto) {
        User user = getAuthenticatedUser();
        ResearchSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (!submission.getResearcher().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You can only update your own submissions");
        }

        if (submission.getSubmissionStatus() != ResearchSubmissionStatus.DRAFT && 
            submission.getSubmissionStatus() != ResearchSubmissionStatus.REJECTED) {
            throw new RuntimeException("Only DRAFT or REJECTED submissions can be updated");
        }

        if (dto.getSubmissionTitle() != null) {
            submission.setSubmissionTitle(dto.getSubmissionTitle());
        }
        if (dto.getSubmissionDescription() != null) {
            submission.setSubmissionDescription(dto.getSubmissionDescription());
        }
        if (dto.getRawStructuredPayload() != null) {
            submission.setRawStructuredPayload(dto.getRawStructuredPayload());
        }

        ResearchSubmission saved = submissionRepository.save(submission);
        return mapToResponse(saved);
    }

    public ResearchSubmissionResponseDTO submitForReview(UUID id) {
        User user = getAuthenticatedUser();
        ResearchSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (!submission.getResearcher().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You can only submit your own submissions");
        }

        if (submission.getSubmissionStatus() != ResearchSubmissionStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT submissions can be submitted for review");
        }

        submission.setSubmissionStatus(ResearchSubmissionStatus.SUBMITTED);
        ResearchSubmission saved = submissionRepository.save(submission);
        return mapToResponse(saved);
    }

    public ResearchSubmissionResponseDTO updateStatus(UUID id, ResearchSubmissionStatus newStatus, String remarks) {
        User reviewer = getAuthenticatedUser();
        if (reviewer.getRole() != Role.ADMIN && reviewer.getRole() != Role.EDITOR) {
            throw new RuntimeException("Only Admins or Editors can review submissions");
        }

        ResearchSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (!submission.getInstitution().getId().equals(reviewer.getUniID().getId()) && reviewer.getRole() != Role.ADMIN) {
            throw new RuntimeException("You can only review submissions for your institution");
        }

        submission.setSubmissionStatus(newStatus);
        submission.setRemarks(remarks);
        submission.setReviewedBy(reviewer);
        submission.setReviewedAt(LocalDateTime.now());

        ResearchSubmission saved = submissionRepository.save(submission);
        return mapToResponse(saved);
    }

    public Page<ResearchSubmissionResponseDTO> getMySubmissions(Pageable pageable) {
        User user = getAuthenticatedUser();
        return submissionRepository.findByResearcher(user, pageable).map(this::mapToResponse);
    }

    public Page<ResearchSubmissionResponseDTO> getInstitutionSubmissions(Pageable pageable) {
        User user = getAuthenticatedUser();
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.EDITOR) {
            throw new RuntimeException("Unauthorized to view institution submissions");
        }
        return submissionRepository.findByInstitution(user.getUniID(), pageable).map(this::mapToResponse);
    }

    public void deleteSubmission(UUID id) {
        User user = getAuthenticatedUser();
        ResearchSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        
        if (!submission.getResearcher().getUserId().equals(user.getUserId()) && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("You can only delete your own submissions");
        }
        
        submissionRepository.delete(submission);
    }

    private ResearchSubmissionResponseDTO mapToResponse(ResearchSubmission submission) {
        ResearchSubmissionResponseDTO dto = new ResearchSubmissionResponseDTO();
        dto.setSubmissionId(submission.getSubmissionId());
        dto.setResearcherId(submission.getResearcher().getUserId());
        dto.setInstitutionId(submission.getInstitution().getId());
        dto.setCivilizationId(submission.getCivilizationReference().getCivId());
        dto.setSubmissionTitle(submission.getSubmissionTitle());
        dto.setSubmissionDescription(submission.getSubmissionDescription());
        dto.setRawStructuredPayload(submission.getRawStructuredPayload());
        dto.setSubmissionStatus(submission.getSubmissionStatus());
        dto.setCreatedAt(submission.getCreatedAt());
        dto.setUpdatedAt(submission.getUpdatedAt());
        dto.setReviewedAt(submission.getReviewedAt());
        dto.setReviewedById(submission.getReviewedBy() != null ? submission.getReviewedBy().getUserId() : null);
        dto.setRemarks(submission.getRemarks());
        return dto;
    }
}
