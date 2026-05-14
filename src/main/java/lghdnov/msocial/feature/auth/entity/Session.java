package lghdnov.msocial.feature.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "auth_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "refresh_token", unique = true, length = 512)
    private String refreshToken;

    @Column(name = "refresh_token_expires_at", nullable = false)
    private Instant refreshTokenExpiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Version
    private Long version;

    public boolean isActive() {
        return revokedAt == null && refreshTokenExpiresAt.isAfter(Instant.now());
    }
}
