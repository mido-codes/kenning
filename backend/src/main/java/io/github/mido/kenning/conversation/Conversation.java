package io.github.mido.kenning.conversation;

import io.github.mido.kenning.document.SourceDocument;
import io.github.mido.kenning.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private SourceDocument document;

    private String title;

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

}
