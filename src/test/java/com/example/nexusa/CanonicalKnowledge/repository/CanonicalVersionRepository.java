package com.example.nexusa.CanonicalKnowledge.repository;
import com.example.nexusa.CanonicalKnowledge.entity.CanonicalVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface CanonicalVersionRepository extends JpaRepository<CanonicalVersion, UUID> {}