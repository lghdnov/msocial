package lghdnov.msocial.feature.comment.repository;

import lghdnov.msocial.feature.comment.entity.Comment;
import lghdnov.msocial.feature.comment.entity.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.status <> :excludedStatus AND c.parentId IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findRootByPostIdAndStatusNot(@Param("postId") Long postId, @Param("excludedStatus") CommentStatus status, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parentId = :parentId AND c.status <> :excludedStatus ORDER BY c.createdAt ASC")
    Page<Comment> findByParentIdAndStatusNot(@Param("parentId") Long parentId, @Param("excludedStatus") CommentStatus status, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.status <> :excludedStatus")
    Optional<Comment> findByIdAndStatusNot(@Param("id") Long id, @Param("excludedStatus") CommentStatus status);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.status <> :excludedStatus")
    long countByPostIdAndStatusNot(@Param("postId") Long postId, @Param("excludedStatus") CommentStatus status);
}
