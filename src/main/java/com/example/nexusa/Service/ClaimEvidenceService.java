package com.example.nexusa.Service;

import com.example.nexusa.Dto.GlobalDTOs.ClaimEvidenceCreateDTO;
import com.example.nexusa.Dto.GlobalDTOs.ClaimEvidenceResponseDTO;
import com.example.nexusa.Model.CitationSource;
import com.example.nexusa.Model.ClaimEvidence;
import com.example.nexusa.Model.HistoricalClaim;
import com.example.nexusa.Model.User;
import com.example.nexusa.Repository.GlobalRepositories.CitationSourceRepository;
import com.example.nexusa.Repository.GlobalRepositories.ClaimEvidenceRepository;
import com.example.nexusa.Repository.GlobalRepositories.HistoricalClaimRepository;
import com.example.nexusa.Repository.GlobalRepositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.example.nexusa.Model.GlobalModels.Citation;

@Service
@org.springframework.transaction.annotation.Transactional
public class ClaimEvidenceService {

    private final ClaimEvidenceRepository evidenceRepository;
    private final HistoricalClaimRepository claimRepository;
    private final CitationSourceRepository citationSourceRepository;
    private final UserRepository userRepository;

    public ClaimEvidenceService(ClaimEvidenceRepository evidenceRepository,
                                HistoricalClaimRepository claimRepository,
                                CitationSourceRepository citationSourceRepository,
                                UserRepository userRepository) {
        this.evidenceRepository = evidenceRepository;
        this.claimRepository = claimRepository;
        this.citationSourceRepository = citationSourceRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        String mail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return userRepository.findUserByEmail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public ClaimEvidenceResponseDTO attachEvidence(ClaimEvidenceCreateDTO dto) {
        User user = getAuthenticatedUser();

        HistoricalClaim claim = claimRepository.findById(dto.getClaimId())
                .orElseThrow(() -> new RuntimeException("Historical claim not found"));

        CitationSource citationSource = citationSourceRepository.findById(dto.getCitationSourceId())
                .orElseThrow(() -> new RuntimeException("Citation source not found"));

        // Validate that evidence text is not trivially short
        if (dto.getEvidenceText().trim().length() < 10) {
            throw new IllegalArgumentException("Evidence text must be at least 10 characters");
        }

        ClaimEvidence evidence = new ClaimEvidence();
        evidence.setClaim(claim);
        evidence.setCitationSource(citationSource);
        evidence.setEvidenceText(dto.getEvidenceText());
        evidence.setQuotedPassage(dto.getQuotedPassage());
        evidence.setPageNumbers(dto.getPageNumbers());
        evidence.setExtractedMetadata(dto.getExtractedMetadata());
        evidence.setUploadedDocuments(dto.getUploadedDocuments());
        evidence.setSubmittedBy(user);

        // Use caller-provided score if given; otherwise inherit from the CitationSource
        evidence.setSourceReliabilityScore(
                dto.getSourceReliabilityScore() != null
                        ? dto.getSourceReliabilityScore()
                        : citationSource.getReliabilityScore()
        );

        return mapToResponse(evidenceRepository.save(evidence));
    }

    public Page<ClaimEvidenceResponseDTO> getEvidenceForClaim(UUID claimId, Pageable pageable) {
        return evidenceRepository.findByClaim_ClaimId(claimId, pageable).map(this::mapToResponse);
    }

    public ClaimEvidenceResponseDTO getEvidence(UUID evidenceId) {
        return evidenceRepository.findById(evidenceId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Evidence not found"));
    }

    /**
     * Returns the aggregate reliability score for all evidence pieces attached to a given claim.
     * This drives the provenance confidence signal on the claim.
     */
    public Double getClaimEvidenceReliabilityScore(UUID claimId) {
        Double avg = evidenceRepository.findAverageReliabilityScoreByClaimId(claimId);
        return avg != null ? avg : 0.0;
    }

    public void deleteEvidence(UUID evidenceId) {
        User user = getAuthenticatedUser();
        ClaimEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found"));
        if (!evidence.getSubmittedBy().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You can only delete your own evidence records");
        }
        evidenceRepository.delete(evidence);
    }

    private ClaimEvidenceResponseDTO mapToResponse(ClaimEvidence evidence) {
        ClaimEvidenceResponseDTO dto = new ClaimEvidenceResponseDTO();
        dto.setEvidenceId(evidence.getEvidenceId());
        dto.setClaimId(evidence.getClaim().getClaimId());
        dto.setCitationSourceId(evidence.getCitationSource().getCitationId());
        dto.setCitationTitle(evidence.getCitationSource().getTitle());
        dto.setEvidenceType(evidence.getCitationSource().getEvidenceType());
        dto.setEvidenceText(evidence.getEvidenceText());
        dto.setQuotedPassage(evidence.getQuotedPassage());
        dto.setPageNumbers(evidence.getPageNumbers());
        dto.setExtractedMetadata(evidence.getExtractedMetadata());
        dto.setUploadedDocuments(evidence.getUploadedDocuments());
        dto.setSourceReliabilityScore(evidence.getSourceReliabilityScore());
        dto.setSubmittedById(evidence.getSubmittedBy().getUserId());
        dto.setCreatedAt(evidence.getCreatedAt());
        return dto;
    }
}
