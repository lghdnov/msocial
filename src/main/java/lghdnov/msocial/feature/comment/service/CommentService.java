package lghdnov.msocial.feature.comment.service;

import lghdnov.msocial.common.exceptions.AccessDeniedException;
import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.feature.comment.api.AuthorLookupPort;
import lghdnov.msocial.feature.comment.api.CommentCommandPort;
import lghdnov.msocial.feature.comment.api.CommentQueryPort;
import lghdnov.msocial.feature.comment.api.PostContextPort;
import lghdnov.msocial.feature.comment.entity.Comment;
import lghdnov.msocial.feature.comment.entity.CommentStatus;
import lghdnov.msocial.feature.comment.presentation.AuthorBasicInfo;
import lghdnov.msocial.feature.comment.presentation.CommentDTO;
import lghdnov.msocial.feature.comment.presentation.CreateCommentRequest;
import lghdnov.msocial.feature.comment.presentation.UpdateCommentRequest;
import lghdnov.msocial.feature.comment.presentation.mapper.CommentMapper;
import lghdnov.msocial.feature.comment.repository.CommentRepository;
import lghdnov.msocial.feature.comment.service.validator.CommentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления комментариями.
 *
 * <p>
 * Реализует {@link CommentQueryPort}, {@link CommentCommandPort}.
 * Оркестрирует работу с JPA-сущностью {@code Comment}, валидацией
 * и кросс-фичевыми портами {@link PostContextPort}, {@link AuthorLookupPort}.
 */
@Service
@RequiredArgsConstructor
class CommentService implements CommentQueryPort, CommentCommandPort {

  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;
  private final CommentValidator commentValidator;
  private final PostContextPort postContextPort;
  private final AuthorLookupPort authorLookupPort;

  @Override
  @Transactional(readOnly = true)
  public Page<CommentDTO> getByPostId(Long postId, Pageable pageable) {
    Page<Comment> comments = commentRepository.findRootByPostIdAndStatusNot(postId, CommentStatus.DELETED, pageable);
    return comments.map(commentMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public CommentDTO getById(Long commentId) {
    Comment comment = findCommentOrThrow(commentId);
    return commentMapper.toDto(comment);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CommentDTO> getReplies(Long parentId, Pageable pageable) {
    findCommentOrThrow(parentId);
    Page<Comment> replies = commentRepository.findByParentIdAndStatusNot(parentId, CommentStatus.DELETED, pageable);
    return replies.map(commentMapper::toDto);
  }

  @Override
  @Transactional
  public CommentDTO create(Long userId, Long postId, CreateCommentRequest request) {
    commentValidator.validateCreate(request);

    if (!postContextPort.isPostVisible(postId)) {
      throw new NotFoundException("POST_NOT_FOUND", "Пост не найден или недоступен");
    }
    if (!postContextPort.allowsComments(postId)) {
      throw new AccessDeniedException("COMMENTS_DISABLED", "Комментирование этого поста отключено");
    }

    if (request.parentId() != null) {
      findCommentOrThrow(request.parentId());
    }

    AuthorBasicInfo authorInfo = authorLookupPort.getAuthorBasicInfo(userId);

    Comment comment = commentMapper.toEntity(request, postId, userId, authorInfo.name());
    Comment saved = commentRepository.save(comment);
    return commentMapper.toDto(saved);
  }

  @Override
  @Transactional
  public CommentDTO update(Long userId, Long commentId, UpdateCommentRequest request) {
    commentValidator.validateUpdate(request);

    Comment comment = findCommentOrThrow(commentId);
    assertAuthorOrThrow(comment, userId);

    comment.editContent(request.content());
    Comment saved = commentRepository.save(comment);
    return commentMapper.toDto(saved);
  }

  @Override
  @Transactional
  public void delete(Long userId, Long commentId) {
    Comment comment = findCommentOrThrow(commentId);
    assertAuthorOrThrow(comment, userId);

    comment.softDelete();
    commentRepository.save(comment);
  }

  private Comment findCommentOrThrow(Long commentId) {
    return commentRepository.findByIdAndStatusNot(commentId, CommentStatus.DELETED)
        .orElseThrow(() -> new NotFoundException("COMMENT_NOT_FOUND", "Комментарий не найден"));
  }

  private void assertAuthorOrThrow(Comment comment, Long userId) {
    if (!comment.isEditableBy(userId)) {
      throw new AccessDeniedException("NOT_AUTHOR", "Вы не являетесь автором этого комментария");
    }
  }
}
