package io.github.mido.kenning.document;

import io.github.mido.kenning.user.User;
import jakarta.annotation.Nullable;
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
public class SourceDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String filename;
    private String contentType;
    private long sizeBytes;


    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Nullable
    private String errorMessage;

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}
