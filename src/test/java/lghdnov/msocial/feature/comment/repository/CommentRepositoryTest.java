package lghdnov.msocial.feature.comment.repository;

import lghdnov.msocial.TestcontainersConfiguration;
import lghdnov.msocial.feature.comment.entity.Comment;
import lghdnov.msocial.feature.comment.entity.CommentStatus;
import lghdnov.msocial.feature.post.entity.Post;
import lghdnov.msocial.feature.post.repository.PostRepository;
import lghdnov.msocial.feature.user.entity.User;
import lghdnov.msocial.feature.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findRootByPostIdAndStatusNot_shouldReturnOnlyRootComments() {
        User user = User.builder().externalId("@test:example.org").build();
        User savedUser = userRepository.save(user);

        Post post = Post.builder()
            .authorId(savedUser.getId())
            .authorName("@test:example.org")
            .content("Post")
            .published(true)
            .build();
        Post savedPost = postRepository.save(post);

        Comment root = Comment.builder()
            .postId(savedPost.getId())
            .authorId(savedUser.getId())
            .authorName("@test:example.org")
            .content("Root comment")
            .status(CommentStatus.PUBLISHED)
            .build();
        Comment savedRoot = commentRepository.save(root);

        Comment reply = Comment.builder()
            .postId(savedPost.getId())
            .authorId(savedUser.getId())
            .authorName("@test:example.org")
            .content("Reply")
            .parentId(savedRoot.getId())
            .status(CommentStatus.PUBLISHED)
            .build();
        commentRepository.save(reply);

        Page<Comment> result = commentRepository.findRootByPostIdAndStatusNot(
            savedPost.getId(), CommentStatus.DELETED, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Root comment");
    }

    @Test
    void findByIdAndStatusNot_shouldReturnEmpty_whenDeleted() {
        User user = User.builder().externalId("@deleted:example.org").build();
        User savedUser = userRepository.save(user);

        Post post = Post.builder()
            .authorId(savedUser.getId())
            .authorName("@deleted:example.org")
            .content("Post")
            .published(true)
            .build();
        Post savedPost = postRepository.save(post);

        Comment comment = Comment.builder()
            .postId(savedPost.getId())
            .authorId(savedUser.getId())
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
        User user = User.builder().externalId("@reply:example.org").build();
        User savedUser = userRepository.save(user);

        Post post = Post.builder()
            .authorId(savedUser.getId())
            .authorName("@reply:example.org")
            .content("Post")
            .published(true)
            .build();
        Post savedPost = postRepository.save(post);

        Comment parent = Comment.builder()
            .postId(savedPost.getId())
            .authorId(savedUser.getId())
            .authorName("@reply:example.org")
            .content("Parent")
            .status(CommentStatus.PUBLISHED)
            .build();
        Comment savedParent = commentRepository.save(parent);

        Comment reply = Comment.builder()
            .postId(savedPost.getId())
            .authorId(savedUser.getId())
            .authorName("@reply:example.org")
            .content("Reply")
            .parentId(savedParent.getId())
            .status(CommentStatus.PUBLISHED)
            .build();
        commentRepository.save(reply);

        Page<Comment> result = commentRepository.findByParentIdAndStatusNot(
            savedParent.getId(), CommentStatus.DELETED, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Reply");
    }

    @Test
    void countByPostIdAndStatusNot_shouldCountOnlyNonDeleted() {
        User user = User.builder().externalId("@count:example.org").build();
        User savedUser = userRepository.save(user);

        Post post = Post.builder()
            .authorId(savedUser.getId())
            .authorName("@count:example.org")
            .content("Post")
            .published(true)
            .build();
        Post savedPost = postRepository.save(post);

        Comment published = Comment.builder()
            .postId(savedPost.getId())
            .authorId(savedUser.getId())
            .authorName("@count:example.org")
            .content("Published")
            .status(CommentStatus.PUBLISHED)
            .build();
        commentRepository.save(published);

        Comment deleted = Comment.builder()
            .postId(savedPost.getId())
            .authorId(savedUser.getId())
            .authorName("@count:example.org")
            .content("Deleted")
            .status(CommentStatus.DELETED)
            .build();
        commentRepository.save(deleted);

        long count = commentRepository.countByPostIdAndStatusNot(savedPost.getId(), CommentStatus.DELETED);

        assertThat(count).isEqualTo(1);
    }
}
