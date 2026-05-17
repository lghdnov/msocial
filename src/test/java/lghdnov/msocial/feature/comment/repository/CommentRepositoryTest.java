package lghdnov.msocial.feature.comment.repository;

import lghdnov.msocial.TestcontainersConfiguration;
import lghdnov.msocial.feature.comment.entity.Comment;
import lghdnov.msocial.feature.comment.entity.CommentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void findRootByPostIdAndStatusNot_shouldReturnOnlyRootComments() {
        Long postId = 999L;
        Comment root = Comment.builder()
            .postId(postId)
            .authorId(1L)
            .authorName("@test:example.org")
            .content("Root comment")
            .status(CommentStatus.PUBLISHED)
            .build();
        Comment savedRoot = commentRepository.save(root);

        Comment reply = Comment.builder()
            .postId(postId)
            .authorId(1L)
            .authorName("@test:example.org")
            .content("Reply")
            .parentId(savedRoot.getId())
            .status(CommentStatus.PUBLISHED)
            .build();
        commentRepository.save(reply);

        Page<Comment> result = commentRepository.findRootByPostIdAndStatusNot(
            postId, CommentStatus.DELETED, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Root comment");
    }

    @Test
    void findByIdAndStatusNot_shouldReturnEmpty_whenDeleted() {
        Comment comment = Comment.builder()
            .postId(1L)
            .authorId(1L)
            .authorName("@deleted:example.org")
            .content("Deleted comment")
            .status(CommentStatus.DELETED)
            .build();
        Comment saved = commentRepository.save(comment);

        Optional<Comment> found = commentRepository.findByIdAndStatusNot(saved.getId(), CommentStatus.DELETED);

        assertThat(found).isEmpty();
    }

    @Test
    void findByParentIdAndStatusNot_shouldReturnReplies() {
        Long parentId = 900L;
        Comment parent = Comment.builder()
            .postId(1L)
            .authorId(1L)
            .authorName("@reply:example.org")
            .content("Parent")
            .status(CommentStatus.PUBLISHED)
            .build();
        commentRepository.save(parent);

        Comment reply = Comment.builder()
            .postId(1L)
            .authorId(1L)
            .authorName("@reply:example.org")
            .content("Reply")
            .parentId(parentId)
            .status(CommentStatus.PUBLISHED)
            .build();
        commentRepository.save(reply);

        Page<Comment> result = commentRepository.findByParentIdAndStatusNot(
            parentId, CommentStatus.DELETED, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Reply");
    }

    @Test
    void countByPostIdAndStatusNot_shouldCountOnlyNonDeleted() {
        Long postId = 800L;
        Comment published = Comment.builder()
            .postId(postId)
            .authorId(1L)
            .authorName("@count:example.org")
            .content("Published")
            .status(CommentStatus.PUBLISHED)
            .build();
        commentRepository.save(published);

        Comment deleted = Comment.builder()
            .postId(postId)
            .authorId(1L)
            .authorName("@count:example.org")
            .content("Deleted")
            .status(CommentStatus.DELETED)
            .build();
        commentRepository.save(deleted);

        long count = commentRepository.countByPostIdAndStatusNot(postId, CommentStatus.DELETED);

        assertThat(count).isEqualTo(1);
    }
}
