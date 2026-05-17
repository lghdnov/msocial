package lghdnov.msocial.feature.post.repository;

import lghdnov.msocial.feature.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.id = ?1 AND p.deleted = false")
    Optional<Post> findById(Long id);

    @Query("SELECT p FROM Post p WHERE p.authorId = ?1 AND p.deleted = false AND p.published = true ORDER BY p.createdAt DESC")
    Page<Post> findAllByAuthorId(Long authorId, Pageable pageable);
}
