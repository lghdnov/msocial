package lghdnov.msocial.feature.comment.repository;

import lghdnov.msocial.feature.comment.entity.Comment;
import lghdnov.msocial.feature.comment.entity.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.postId = ?1 AND c.status <> ?2 AND c.parentId IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findRootByPostIdAndStatusNot(Long postId, CommentStatus status, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parentId = ?1 AND c.status <> ?2 ORDER BY c.createdAt ASC")
    Page<Comment> findByParentIdAndStatusNot(Long parentId, CommentStatus status, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.id = ?1 AND c.status <> ?2")
    Optional<Comment> findByIdAndStatusNot(Long id, CommentStatus status);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = ?1 AND c.status <> ?2")
    long countByPostIdAndStatusNot(Long postId, CommentStatus status);
}
