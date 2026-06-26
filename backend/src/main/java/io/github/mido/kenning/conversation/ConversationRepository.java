package io.github.mido.kenning.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByUserId(UUID userId);

    Optional<Conversation> findByIdAndUserId(UUID id, UUID userId);

    List<Conversation> findByDocumentId(UUID documentId);

    Optional<Conversation> findByDocumentIdAndUserId(UUID documentId, UUID userId);
}
