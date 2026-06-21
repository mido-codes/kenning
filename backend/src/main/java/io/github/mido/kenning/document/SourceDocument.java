package io.github.mido.kenning.document;

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
