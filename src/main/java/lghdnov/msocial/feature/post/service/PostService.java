package lghdnov.msocial.feature.post.service;

import lghdnov.msocial.common.exceptions.AccessDeniedException;
import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.feature.post.api.PostCommandPort;
import lghdnov.msocial.feature.post.api.PostMediaStoragePort;
import lghdnov.msocial.feature.post.api.PostQueryPort;
import lghdnov.msocial.feature.post.entity.Post;
import lghdnov.msocial.feature.post.entity.PostMedia;
import lghdnov.msocial.feature.post.presentation.CreatePostRequest;
import lghdnov.msocial.feature.post.presentation.PostDTO;
import lghdnov.msocial.feature.post.presentation.UpdatePostRequest;
import lghdnov.msocial.feature.post.presentation.mapper.PostMapper;
import lghdnov.msocial.feature.post.repository.PostMediaRepository;
import lghdnov.msocial.feature.post.repository.PostRepository;
import lghdnov.msocial.feature.post.service.PostMediaValidator;
import lghdnov.msocial.feature.user.api.UserQueryPort;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис управления постами.
 *
 * <p>Реализует {@link PostQueryPort}, {@link PostCommandPort}.
 * Оркестрирует работу с JPA-сущностями {@code Post}, {@code PostMedia}
 * и медиа-хранилищем через {@link PostMediaStoragePort}.
 */
@Service
class PostService implements PostQueryPort, PostCommandPort {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final PostMapper postMapper;
    private final UserQueryPort userQueryPort;
    private final PostMediaStoragePort postMediaStoragePort;
    private final PostMediaValidator postMediaValidator;

    PostService(
        PostRepository postRepository,
        PostMediaRepository postMediaRepository,
        PostMapper postMapper,
        UserQueryPort userQueryPort,
        PostMediaStoragePort postMediaStoragePort,
        PostMediaValidator postMediaValidator
    ) {
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.postMapper = postMapper;
        this.userQueryPort = userQueryPort;
        this.postMediaStoragePort = postMediaStoragePort;
        this.postMediaValidator = postMediaValidator;
    }

    @Override
    @Transactional(readOnly = true)
    public PostDTO getPost(Long userId, Long postId) {
        Post post = findPostOrThrow(postId);
        if (!post.getPublished() && !post.getAuthorId().equals(userId)) {
            throw new NotFoundException("POST_NOT_FOUND", "Пост не найден");
        }
        List<PostMedia> media = postMediaRepository.findByPostIdOrderBySortOrderAsc(postId);
        return postMapper.toDto(post, media);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDTO> getFeed(Long authorId, Pageable pageable) {
        Page<Post> posts = postRepository.findAllByAuthorId(authorId, pageable);
        List<Long> postIds = posts.getContent().stream()
            .map(Post::getId)
            .toList();

        Map<Long, List<PostMedia>> mediaMap = postMediaRepository.findByPostIdInOrderBySortOrderAsc(postIds)
            .stream()
            .collect(Collectors.groupingBy(PostMedia::getPostId));

        return posts.map(post -> {
            List<PostMedia> media = mediaMap.getOrDefault(post.getId(), List.of());
            return postMapper.toDto(post, media);
        });
    }

    @Override
    @Transactional
    public PostDTO createPost(Long authorId, CreatePostRequest request) {
        UserDTO userDTO = userQueryPort.getProfile(authorId);

        Post post = Post.builder()
            .authorId(authorId)
            .authorName(userDTO.externalId())
            .content(request.content())
            .published(false)
            .build();

        Post saved = postRepository.save(post);
        return postMapper.toDto(saved, List.of());
    }

    @Override
    @Transactional
    public PostDTO updatePost(Long userId, Long postId, UpdatePostRequest request) {
        Post post = findPostOrThrow(postId);
        assertAuthorOrThrow(post, userId);

        post.updateContent(request.content());
        Post saved = postRepository.save(post);
        List<PostMedia> media = postMediaRepository.findByPostIdOrderBySortOrderAsc(postId);
        return postMapper.toDto(saved, media);
    }

    @Override
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = findPostOrThrow(postId);
        assertAuthorOrThrow(post, userId);

        post.markDeleted();
        postRepository.save(post);
    }

    @Override
    @Transactional
    public PostDTO publishPost(Long userId, Long postId) {
        Post post = findPostOrThrow(postId);
        assertAuthorOrThrow(post, userId);

        post.publish();
        Post saved = postRepository.save(post);
        List<PostMedia> media = postMediaRepository.findByPostIdOrderBySortOrderAsc(postId);
        return postMapper.toDto(saved, media);
    }

    @Override
    @Transactional
    public PostDTO addPostMedia(Long userId, Long postId, List<MultipartFile> files) {
        Post post = findPostOrThrow(postId);
        assertAuthorOrThrow(post, userId);

        List<PostMedia> existingMedia = postMediaRepository.findByPostIdOrderBySortOrderAsc(postId);
        postMediaValidator.validate(files, existingMedia.size());

        int startOrder = existingMedia.size();
        List<String> uploadedUrls = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String url = postMediaStoragePort.uploadPostMedia(file);
            uploadedUrls.add(url);
            registerRollbackCleanup(url);

            PostMedia media = PostMedia.builder()
                .postId(postId)
                .url(url)
                .sortOrder(startOrder + i)
                .build();
            postMediaRepository.save(media);
        }

        List<PostMedia> allMedia = postMediaRepository.findByPostIdOrderBySortOrderAsc(postId);
        return postMapper.toDto(post, allMedia);
    }

    @Override
    @Transactional
    public void deletePostMedia(Long userId, Long postId, Long mediaId) {
        Post post = findPostOrThrow(postId);
        assertAuthorOrThrow(post, userId);

        PostMedia media = postMediaRepository.findById(mediaId)
            .orElseThrow(() -> new NotFoundException("MEDIA_NOT_FOUND", "Медиафайл не найден"));

        if (!media.getPostId().equals(postId)) {
            throw new lghdnov.msocial.common.exceptions.ValidationException("MEDIA_NOT_IN_POST", "Медиафайл не принадлежит этому посту");
        }

        String url = media.getUrl();
        postMediaRepository.delete(media);

        try {
            postMediaStoragePort.delete(url);
        } catch (Exception e) {
            log.warn("Не удалось удалить медиафайл из хранилища после удаления записи из БД: url={}, error={}", url, e.getMessage());
        }
    }

    private Post findPostOrThrow(Long postId) {
        return postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("POST_NOT_FOUND", "Пост не найден"));
    }

    private void assertAuthorOrThrow(Post post, Long userId) {
        if (!post.getAuthorId().equals(userId)) {
            throw new AccessDeniedException("NOT_AUTHOR", "Вы не являетесь автором этого поста");
        }
    }

    private void registerRollbackCleanup(String url) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        postMediaStoragePort.delete(url);
                    }
                }
            });
        }
    }
}
