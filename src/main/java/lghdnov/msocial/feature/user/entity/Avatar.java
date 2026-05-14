package lghdnov.msocial.feature.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "avatars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avatar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = Instant.now();
        }
        if (active == null) {
            active = true;
        }
    }
}
