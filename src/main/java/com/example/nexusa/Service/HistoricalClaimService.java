package com.example.nexusa.Service;

import com.example.nexusa.Dto.GlobalDTOs.HistoricalClaimCreateDTO;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalClaimResponseDTO;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalClaimUpdateDTO;
import com.example.nexusa.Model.Enums.GlobalEnums.ClaimStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.Role;
import com.example.nexusa.Model.HistoricalClaim;
import com.example.nexusa.Model.ResearchSubmission;
import com.example.nexusa.Model.User;
import com.example.nexusa.Repository.GlobalRepositories.HistoricalClaimRepository;
import com.example.nexusa.Repository.GlobalRepositories.ResearchSubmissionRepository;
import com.example.nexusa.Repository.GlobalRepositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@org.springframework.transaction.annotation.Transactional
public class HistoricalClaimService {

    private final HistoricalClaimRepository claimRepository;
    private final ResearchSubmissionRepository submissionRepository;
    private final UserRepository userRepository;

    public HistoricalClaimService(HistoricalClaimRepository claimRepository,
                                  ResearchSubmissionRepository submissionRepository,
                                  UserRepository userRepository) {
        this.claimRepository = claimRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        String mail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return userRepository.findUserByEmail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public HistoricalClaimResponseDTO createClaim(HistoricalClaimCreateDTO dto) {
        User user = getAuthenticatedUser();
        
        HistoricalClaim claim = new HistoricalClaim();
        claim.setSubjectEntityId(dto.getSubjectEntityId());
        claim.setSubjectEntityType(dto.getSubjectEntityType());
        claim.setPredicate(dto.getPredicate());
        claim.setObjectValue(dto.getObjectValue());
        claim.setNormalizedValue(dto.getNormalizedValue());
        claim.setClaimType(dto.getClaimType());
        claim.setConfidenceScore(dto.getConfidenceScore());
        claim.setSubmittedBy(user);
        claim.setClaimStatus(ClaimStatus.PENDING);

        if (dto.getSubmissionId() != null) {
            ResearchSubmission submission = submissionRepository.findById(dto.getSubmissionId())
                    .orElseThrow(() -> new RuntimeException("Submission not found"));
            claim.setSubmission(submission);
        }

        HistoricalClaim saved = claimRepository.save(claim);
        return mapToResponse(saved);
    }

    @Transactional
    public HistoricalClaimResponseDTO updateClaim(UUID claimId, HistoricalClaimUpdateDTO dto) {
        User user = getAuthenticatedUser();
        HistoricalClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        if (!claim.getSubmittedBy().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You can only edit your own claims");
        }

        if (claim.getClaimStatus() != ClaimStatus.PENDING && claim.getClaimStatus() != ClaimStatus.DISPUTED) {
            throw new RuntimeException("Cannot edit validated or canonical claims");
        }

        if (dto.getPredicate() != null) claim.setPredicate(dto.getPredicate());
        if (dto.getObjectValue() != null) claim.setObjectValue(dto.getObjectValue());
        if (dto.getNormalizedValue() != null) claim.setNormalizedValue(dto.getNormalizedValue());
        if (dto.getClaimType() != null) claim.setClaimType(dto.getClaimType());
        if (dto.getConfidenceScore() != null) claim.setConfidenceScore(dto.getConfidenceScore());

        return mapToResponse(claimRepository.save(claim));
    }

    @Transactional
    public HistoricalClaimResponseDTO moderateClaim(UUID claimId, ClaimStatus newStatus, String notes) {
        User moderator = getAuthenticatedUser();
        if (moderator.getRole() != Role.ADMIN && moderator.getRole() != Role.EDITOR) {
            throw new RuntimeException("Unauthorized to moderate claims");
        }

        HistoricalClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));

        claim.setClaimStatus(newStatus);
        claim.setModerationNotes(notes);

        if (newStatus == ClaimStatus.CANONICAL) {
            claim.setCanonicalFlag(true);
            
            // Supersede conflicting canonical claims
            List<HistoricalClaim> conflictingClaims = claimRepository.findBySubjectEntityIdAndPredicate(
                    claim.getSubjectEntityId(), claim.getPredicate());
            
            for (HistoricalClaim conflicting : conflictingClaims) {
                if (!conflicting.getClaimId().equals(claim.getClaimId()) && conflicting.isCanonicalFlag()) {
                    conflicting.setCanonicalFlag(false);
                    conflicting.setClaimStatus(ClaimStatus.SUPERSEDED);
                    conflicting.setModerationNotes("Superseded by claim " + claim.getClaimId());
                    claimRepository.save(conflicting);
                }
            }
        } else {
            claim.setCanonicalFlag(false);
        }

        return mapToResponse(claimRepository.save(claim));
    }

    public Page<HistoricalClaimResponseDTO> getClaimsBySubmission(UUID submissionId, Pageable pageable) {
        return claimRepository.findBySubmission_SubmissionId(submissionId, pageable).map(this::mapToResponse);
    }

    public Page<HistoricalClaimResponseDTO> getClaimsBySubject(UUID subjectId, Pageable pageable) {
        return claimRepository.findBySubjectEntityId(subjectId, pageable).map(this::mapToResponse);
    }

    public HistoricalClaimResponseDTO getClaim(UUID claimId) {
        return claimRepository.findById(claimId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
    }

    private HistoricalClaimResponseDTO mapToResponse(HistoricalClaim claim) {
        HistoricalClaimResponseDTO dto = new HistoricalClaimResponseDTO();
        dto.setClaimId(claim.getClaimId());
        dto.setSubmissionId(claim.getSubmission() != null ? claim.getSubmission().getSubmissionId() : null);
        dto.setSubjectEntityId(claim.getSubjectEntityId());
        dto.setSubjectEntityType(claim.getSubjectEntityType());
        dto.setPredicate(claim.getPredicate());
        dto.setObjectValue(claim.getObjectValue());
        dto.setNormalizedValue(claim.getNormalizedValue());
        dto.setClaimType(claim.getClaimType());
        dto.setClaimStatus(claim.getClaimStatus());
        dto.setConfidenceScore(claim.getConfidenceScore());
        dto.setSubmittedById(claim.getSubmittedBy().getUserId());
        dto.setCreatedAt(claim.getCreatedAt());
        dto.setUpdatedAt(claim.getUpdatedAt());
        dto.setCanonicalFlag(claim.isCanonicalFlag());
        dto.setModerationNotes(claim.getModerationNotes());
        return dto;
    }
}
