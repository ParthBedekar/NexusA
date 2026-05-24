package com.example.nexusa.Service;

import com.example.nexusa.Dto.AddNodeRequestDTO;
import com.example.nexusa.Dto.CreateCivilizationDTO;
import com.example.nexusa.Model.CVersion;
import com.example.nexusa.Model.Civilization;
import com.example.nexusa.Model.EditorAssignment;
import com.example.nexusa.Model.Enums.ReviewStatus;
import com.example.nexusa.Model.Enums.Role;
import com.example.nexusa.Model.User;
import com.example.nexusa.Repository.CVersionRepository;
import com.example.nexusa.Repository.CivilizationRepository;
import com.example.nexusa.Repository.EditorAssignmentRepository;
import com.example.nexusa.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class CivilizationService {
    private final UserRepository userRepository;
    private final CivilizationRepository civilizationRepository;
    private final CVersionRepository cVersionRepository;
    private final CMETreeService cmeTreeService;
    private final EditorAssignmentRepository editorAssignmentRepository;

    public CivilizationService(UserRepository userRepository,
                               CivilizationRepository civilizationRepository,
                               CVersionRepository cVersionRepository,
                               CMETreeService cmeTreeService,
                               EditorAssignmentRepository editorAssignmentRepository) {
        this.userRepository = userRepository;
        this.civilizationRepository = civilizationRepository;
        this.cVersionRepository = cVersionRepository;
        this.cmeTreeService = cmeTreeService;
        this.editorAssignmentRepository = editorAssignmentRepository;
    }

    private User getAuthenticatedUser() {
        String mail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return userRepository.findUserByEmail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void assertIsEditor(User user, UUID civId) {
        if (user.getRole() == Role.ADMIN) return;
        if (!editorAssignmentRepository.existsByCivilization_CivIdAndEditor_UserId(civId, user.getUserId())) {
            throw new RuntimeException("You are not an editor for this civilization");
        }
    }

    public UUID createCivilization(CreateCivilizationDTO createCivilizationDTO) {
        User uniAdmin = getAuthenticatedUser();
        if (uniAdmin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only admins can create civilizations");
        }
        Civilization civilization = new Civilization();
        civilization.setTitle(createCivilizationDTO.getTitle());
        civilization.setDescription(createCivilizationDTO.getDescription());
        civilization.setCreatedBy(uniAdmin);
        civilization.setStartDate(createCivilizationDTO.getStartYear());
        civilization.setEndDate(createCivilizationDTO.getEndYear());
        civilization.setUniversity(uniAdmin.getUniID());
        civilizationRepository.save(civilization);
        try {
            CVersion initialVersion = cmeTreeService.createInitialVersion(civilization, createCivilizationDTO.getCommitMsg(), uniAdmin);
            cVersionRepository.save(initialVersion);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return civilization.getCivId();
    }
    public void submitForReview(UUID civId, UUID versionId) {
        User admin = getAuthenticatedUser();
        if (admin.getRole() != Role.ADMIN)
            throw new RuntimeException("Only admins can submit for review");

        CVersion version = cVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));
        if (!version.getCivilization().getCivId().equals(civId))
            throw new RuntimeException("Version does not belong to this civilization");
        if (version.getReviewStatus() != ReviewStatus.DRAFT &&
                version.getReviewStatus() != ReviewStatus.REVISION_REQUESTED)
            throw new RuntimeException("Only DRAFT or REVISION_REQUESTED versions can be submitted");

        version.setReviewStatus(ReviewStatus.PENDING_REVIEW);
        cVersionRepository.save(version);
    }

    public void reviewVersion(UUID versionId, ReviewStatus decision, String note) {
        User reviewer = getAuthenticatedUser();
        if (reviewer.getRole() != Role.REVIEWER)
            throw new RuntimeException("Only reviewers can review versions");
        if (decision == ReviewStatus.DRAFT || decision == ReviewStatus.PENDING_REVIEW)
            throw new RuntimeException("Invalid review decision");

        CVersion version = cVersionRepository.findById(versionId)
                .orElseThrow(() -> new RuntimeException("Version not found"));
        if (version.getReviewStatus() != ReviewStatus.PENDING_REVIEW)
            throw new RuntimeException("Version is not pending review");

        version.setReviewStatus(decision);
        version.setReviewedBy(reviewer);
        version.setReviewedAt(LocalDateTime.now());
        version.setReviewerNote(note);
        cVersionRepository.save(version);
    }

    public List<CVersion> getPendingVersions() {
        User reviewer = getAuthenticatedUser();
        if (reviewer.getRole() != Role.REVIEWER)
            throw new RuntimeException("Only reviewers can access this");
        return cVersionRepository.findByReviewStatus(ReviewStatus.PENDING_REVIEW);
    }
    public List<User> getUniversityUsers() {
        User admin = getAuthenticatedUser();
        return userRepository.findByUniID(admin.getUniID());
    }

    public CVersion getLatestVersion(UUID civId) {
        return cVersionRepository.findTopByCivilization_CivIdOrderByCommitTimestampDesc(civId)
                .orElseThrow(() -> new RuntimeException("No versions found"));
    }

    public void assignEditor(UUID civId, UUID userId) {
        User admin = getAuthenticatedUser();
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only admins can assign editors");
        }
        Civilization civ = civilizationRepository.findById(civId)
                .orElseThrow(() -> new RuntimeException("Civilization not found"));
        User editor = userRepository.findUserByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (editorAssignmentRepository.existsByCivilization_CivIdAndEditor_UserId(civId, userId)) {
            throw new RuntimeException("User already an editor for this civilization");
        }
        EditorAssignment assignment = new EditorAssignment();
        assignment.setCivilization(civ);
        assignment.setEditor(editor);
        editor.setRole(Role.EDITOR);
        assignment.setAssignedBy(admin);
        assignment.setAssignedAt(LocalDateTime.now());
        editorAssignmentRepository.save(assignment);
    }
    @Transactional
    public void deleteCivilization(UUID civId) {
        User admin = getAuthenticatedUser();
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only admins can delete civilizations");
        }
        Civilization civ = civilizationRepository.findById(civId)
                .orElseThrow(() -> new RuntimeException("Civilization not found"));

        // Compare University IDs, not object references
        if (!civ.getUniversity().getId().equals(admin.getUniID().getId())) {
            throw new RuntimeException("Civilization does not belong to your university");
        }

        editorAssignmentRepository.deleteByCivilization_CivId(civId);
        cVersionRepository.deleteByCivilization_CivId(civId);
        civilizationRepository.deleteById(civId);
    }
    public List<EditorAssignment> getCivilizationEditors(UUID civId) {
        return editorAssignmentRepository.findByCivilization_CivId(civId);
    }

    public List<Civilization> getMyCivilizations() {
        User editor = getAuthenticatedUser();
        return editorAssignmentRepository.findByEditor_UserId(editor.getUserId())
                .stream()
                .map(EditorAssignment::getCivilization)
                .toList();
    }

    public CVersion addVolume(UUID civId, AddNodeRequestDTO dto) {
        User user = getAuthenticatedUser();
        assertIsEditor(user, civId);
        CVersion version = cmeTreeService.addVolume(civId, dto.getParentNodeId(), dto.getNode(), dto.getCommitMsg(), user);
        return cVersionRepository.save(version);
    }

    public CVersion addEntry(UUID civId, AddNodeRequestDTO dto) {
        User user = getAuthenticatedUser();
        assertIsEditor(user, civId);
        CVersion version = cmeTreeService.addEntry(civId, dto.getParentNodeId(), dto.getNode(), dto.getCommitMsg(), user);
        return cVersionRepository.save(version);
    }

    public CVersion updateNode(UUID civId, UUID nodeId, AddNodeRequestDTO dto) {
        User user = getAuthenticatedUser();
        assertIsEditor(user, civId);
        CVersion version = cmeTreeService.updateEntry(civId, nodeId, dto.getNode(), dto.getCommitMsg(), user);
        return cVersionRepository.save(version);
    }

    public CVersion rollback(UUID civId, String hash) {
        User user = getAuthenticatedUser();
        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only admins can rollback");
        }
        CVersion version = cmeTreeService.rollback(civId, hash, user);
        return cVersionRepository.save(version);
    }

    public List<CVersion> getAllVersions(UUID civId) {
        return cVersionRepository.findByCivilization_CivIdOrderByCommitTimestampDesc(civId);
    }

    // In CivilizationService.java
    public List<Civilization> getAllUniversityCivilizations() {
        User admin = getAuthenticatedUser();
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only admins can access this");
        }
        return civilizationRepository.findByUniversity(admin.getUniID());
    }

    // CivilizationService
    public List<CVersion> getReviewedVersions() {
        User reviewer = getAuthenticatedUser();
        if (reviewer.getRole() != Role.REVIEWER)
            throw new RuntimeException("Only reviewers can access this");
        return cVersionRepository.findByReviewedBy_UserId(reviewer.getUserId());
    }
}