package io.github.mido.kenning.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<SourceDocument, UUID> {
    List<SourceDocument> findByUserId(UUID userId);

    Optional<SourceDocument> findByIdAndUserId(UUID id, UUID userId);
}
