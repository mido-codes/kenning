package io.github.mido.kenning.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String googleSub;

    @Column(nullable = false)
    private String email;

    private String displayName;

    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}