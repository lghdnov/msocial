package lghdnov.msocial.feature.comment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comments_post_id_status", columnList = "post_id, status"),
    @Index(name = "idx_comments_parent_id_status", columnList = "parent_id, status"),
    @Index(name = "idx_comments_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "author_name", nullable = false, length = 255)
    private String authorName;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private CommentStatus status = CommentStatus.PUBLISHED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = CommentStatus.PUBLISHED;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public void editContent(String newContent) {
        this.content = newContent;
    }

    public void softDelete() {
        this.status = CommentStatus.DELETED;
    }

    public boolean isEditableBy(Long userId) {
        return this.authorId.equals(userId);
    }
}
