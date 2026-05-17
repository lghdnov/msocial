package lghdnov.msocial.feature.post.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_media", indexes = {
    @Index(name = "idx_post_media_post_id", columnList = "post_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Version
    private Long version;
}
