package com.example.nexusa.Service;

import com.example.nexusa.Dto.GlobalDTOs.CitationSourceCreateDTO;
import com.example.nexusa.Dto.GlobalDTOs.CitationSourceResponseDTO;
import com.example.nexusa.Model.CitationSource;
import com.example.nexusa.Model.Enums.GlobalEnums.EvidenceType;
import com.example.nexusa.Model.User;
import com.example.nexusa.Repository.GlobalRepositories.CitationSourceRepository;
import com.example.nexusa.Repository.GlobalRepositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import com.example.nexusa.Model.GlobalModels.Citation;

@Service
@org.springframework.transaction.annotation.Transactional
public class CitationSourceService {

    private final CitationSourceRepository citationSourceRepository;
    private final UserRepository userRepository;

    public CitationSourceService(CitationSourceRepository citationSourceRepository,
                                 UserRepository userRepository) {
        this.citationSourceRepository = citationSourceRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        String mail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return userRepository.findUserByEmail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public CitationSourceResponseDTO createCitationSource(CitationSourceCreateDTO dto) {
        User user = getAuthenticatedUser();

        // Prevent duplicate DOI registrations
        if (dto.getDoi() != null && !dto.getDoi().isBlank()) {
            Optional<CitationSource> existing = citationSourceRepository.findByDoi(dto.getDoi());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("A citation with DOI '" + dto.getDoi() + "' already exists (ID: " + existing.get().getCitationId() + ")");
            }
        }

        // Prevent duplicate ISBN registrations
        if (dto.getIsbn() != null && !dto.getIsbn().isBlank()) {
            Optional<CitationSource> existing = citationSourceRepository.findByIsbn(dto.getIsbn());
            if (existing.isPresent()) {
                throw new IllegalArgumentException("A citation with ISBN '" + dto.getIsbn() + "' already exists (ID: " + existing.get().getCitationId() + ")");
            }
        }

        CitationSource source = new CitationSource();
        source.setEvidenceType(dto.getEvidenceType());
        source.setTitle(dto.getTitle());
        source.setAuthors(dto.getAuthors());
        source.setPublicationYear(dto.getPublicationYear());
        source.setPublisher(dto.getPublisher());
        source.setJournalName(dto.getJournalName());
        source.setDoi(dto.getDoi());
        source.setIsbn(dto.getIsbn());
        source.setUrl(dto.getUrl());
        source.setArchiveName(dto.getArchiveName());
        source.setArchiveLocation(dto.getArchiveLocation());
        source.setExtendedMetadata(dto.getExtendedMetadata());
        source.setReliabilityScore(dto.getReliabilityScore() != null ? dto.getReliabilityScore() : 0.5);
        source.setSubmittedBy(user);

        return mapToResponse(citationSourceRepository.save(source));
    }

    public CitationSourceResponseDTO getCitationSource(UUID id) {
        return citationSourceRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Citation source not found"));
    }

    public Page<CitationSourceResponseDTO> getByType(EvidenceType type, Pageable pageable) {
        return citationSourceRepository.findByEvidenceType(type, pageable).map(this::mapToResponse);
    }

    public Page<CitationSourceResponseDTO> getHighReliabilitySources(Double minScore, Pageable pageable) {
        return citationSourceRepository.findByReliabilityScoreGreaterThanEqual(minScore, pageable).map(this::mapToResponse);
    }

    public CitationSourceResponseDTO updateReliabilityScore(UUID id, Double newScore) {
        User user = getAuthenticatedUser();
        if (newScore < 0.0 || newScore > 1.0) {
            throw new IllegalArgumentException("Reliability score must be between 0.0 and 1.0");
        }
        CitationSource source = citationSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Citation source not found"));
        source.setReliabilityScore(newScore);
        return mapToResponse(citationSourceRepository.save(source));
    }

    private CitationSourceResponseDTO mapToResponse(CitationSource source) {
        CitationSourceResponseDTO dto = new CitationSourceResponseDTO();
        dto.setCitationId(source.getCitationId());
        dto.setEvidenceType(source.getEvidenceType());
        dto.setTitle(source.getTitle());
        dto.setAuthors(source.getAuthors());
        dto.setPublicationYear(source.getPublicationYear());
        dto.setPublisher(source.getPublisher());
        dto.setJournalName(source.getJournalName());
        dto.setDoi(source.getDoi());
        dto.setIsbn(source.getIsbn());
        dto.setUrl(source.getUrl());
        dto.setArchiveName(source.getArchiveName());
        dto.setArchiveLocation(source.getArchiveLocation());
        dto.setExtendedMetadata(source.getExtendedMetadata());
        dto.setReliabilityScore(source.getReliabilityScore());
        dto.setSubmittedById(source.getSubmittedBy().getUserId());
        dto.setCreatedAt(source.getCreatedAt());
        return dto;
    }
}
