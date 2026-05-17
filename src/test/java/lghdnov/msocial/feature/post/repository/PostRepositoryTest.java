package lghdnov.msocial.feature.post.repository;

import lghdnov.msocial.TestcontainersConfiguration;
import lghdnov.msocial.feature.post.entity.Post;
import lghdnov.msocial.feature.post.entity.PostMedia;
import lghdnov.msocial.feature.user.entity.User;
import lghdnov.msocial.feature.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findById_shouldReturnPost_whenNotDeleted() {
        User user = User.builder().externalId("@test:example.org").build();
        User savedUser = userRepository.save(user);

        Post post = Post.builder()
            .authorId(savedUser.getId())
            .authorName("@test:example.org")
            .content("Hello")
            .published(true)
            .build();
        Post saved = postRepository.save(post);

        Optional<Post> found = postRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("Hello");
    }

    @Test
    void findById_shouldReturnEmpty_whenDeleted() {
        User user = User.builder().externalId("@deleted:example.org").build();
        User savedUser = userRepository.save(user);

        Post post = Post.builder()
            .authorId(savedUser.getId())
            .authorName("@deleted:example.org")
            .content("Hello")
            .deleted(true)
            .published(true)
            .build();
        Post saved = postRepository.save(post);

        Optional<Post> found = postRepository.findById(saved.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void findAllByAuthorId_shouldReturnOnlyPublished() {
        User user = User.builder().externalId("@feed:example.org").build();
        User savedUser = userRepository.save(user);

        Post published = Post.builder()
            .authorId(savedUser.getId())
            .authorName("@feed:example.org")
            .content("Published post")
            .published(true)
            .build();
        postRepository.save(published);

        Post unpublished = Post.builder()
            .authorId(savedUser.getId())
            .authorName("@feed:example.org")
            .content("Draft post")
            .published(false)
            .build();
        postRepository.save(unpublished);

        Page<Post> result = postRepository.findAllByAuthorId(savedUser.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("Published post");
    }

    @Test
    void postMedia_shouldLinkToPost() {
        User user = User.builder().externalId("@media:example.org").build();
        User savedUser = userRepository.save(user);

        Post post = Post.builder()
            .authorId(savedUser.getId())
            .authorName("@media:example.org")
            .content("Media post")
            .published(true)
            .build();
        Post savedPost = postRepository.save(post);

        PostMedia media = PostMedia.builder()
            .postId(savedPost.getId())
            .url("/post-media/test.jpg")
            .sortOrder(0)
            .build();
        postMediaRepository.save(media);

        List<PostMedia> mediaList = postMediaRepository.findByPostIdOrderBySortOrderAsc(savedPost.getId());

        assertThat(mediaList).hasSize(1);
        assertThat(mediaList.get(0).getUrl()).isEqualTo("/post-media/test.jpg");
    }
}
