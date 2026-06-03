package com.example.nexusa.Repository.Central;

import com.example.nexusa.Model.Central.CentralEntryDivergence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CentralEntryDivergenceRepository extends JpaRepository<CentralEntryDivergence, UUID> {

    // All divergences involving a specific entry (as primary or conflicting)
    List<CentralEntryDivergence> findByPrimaryEntry_CentralEntryId(UUID entryId);
    List<CentralEntryDivergence> findByConflictingEntry_CentralEntryId(UUID entryId);

    // Check if a divergence already exists between two entries
    boolean existsByPrimaryEntry_CentralEntryIdAndConflictingEntry_CentralEntryId(
            UUID primaryEntryId, UUID conflictingEntryId);
}