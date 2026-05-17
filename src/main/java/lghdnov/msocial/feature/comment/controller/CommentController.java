package lghdnov.msocial.feature.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lghdnov.msocial.common.exceptions.AccessDeniedException;
import lghdnov.msocial.common.exceptions.ErrorResponse;
import lghdnov.msocial.feature.comment.api.CommentCommandPort;
import lghdnov.msocial.feature.comment.api.CommentQueryPort;
import lghdnov.msocial.feature.comment.presentation.CommentDTO;
import lghdnov.msocial.feature.comment.presentation.CreateCommentRequest;
import lghdnov.msocial.feature.comment.presentation.UpdateCommentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "Comments", description = "Управление комментариями")
@RestController
public class CommentController {

    private final CommentQueryPort commentQueryPort;
    private final CommentCommandPort commentCommandPort;

    public CommentController(CommentQueryPort commentQueryPort, CommentCommandPort commentCommandPort) {
        this.commentQueryPort = commentQueryPort;
        this.commentCommandPort = commentCommandPort;
    }

    @Operation(summary = "Получить комментарии к посту")
    @ApiResponse(responseCode = "200", description = "Список комментариев",
        content = @Content(schema = @Schema(implementation = Page.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<Page<CommentDTO>> getComments(
        @Parameter(description = "Идентификатор поста", required = true, example = "1")
        @PathVariable Long postId,
        @Parameter(hidden = true) Pageable pageable
    ) {
        return ResponseEntity.ok(commentQueryPort.getByPostId(postId, pageable));
    }

    @Operation(summary = "Создать комментарий к посту")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Комментарий создан",
        content = @Content(schema = @Schema(implementation = CommentDTO.class)))
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Комментирование запрещено",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пост не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/api/v1/posts/{postId}/comments")
    public ResponseEntity<CommentDTO> createComment(
        Principal principal,
        @Parameter(description = "Идентификатор поста", required = true, example = "1")
        @PathVariable Long postId,
        @Valid @RequestBody CreateCommentRequest request
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentCommandPort.create(userId, postId, request));
    }

    @Operation(summary = "Получить комментарий по ID")
    @ApiResponse(responseCode = "200", description = "Комментарий найден",
        content = @Content(schema = @Schema(implementation = CommentDTO.class)))
    @ApiResponse(responseCode = "404", description = "Комментарий не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<CommentDTO> getComment(
        @Parameter(description = "Идентификатор комментария", required = true, example = "1")
        @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(commentQueryPort.getById(commentId));
    }

    @Operation(summary = "Обновить комментарий")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Комментарий обновлён",
        content = @Content(schema = @Schema(implementation = CommentDTO.class)))
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Нет доступа",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Комментарий не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
        Principal principal,
        @Parameter(description = "Идентификатор комментария", required = true, example = "1")
        @PathVariable Long commentId,
        @Valid @RequestBody UpdateCommentRequest request
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(commentCommandPort.update(userId, commentId, request));
    }

    @Operation(summary = "Удалить комментарий")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Комментарий удалён")
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Нет доступа",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Комментарий не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/api/v1/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
        Principal principal,
        @Parameter(description = "Идентификатор комментария", required = true, example = "1")
        @PathVariable Long commentId
    ) {
        Long userId = resolveUserId(principal);
        commentCommandPort.delete(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить ответы на комментарий")
    @ApiResponse(responseCode = "200", description = "Список ответов",
        content = @Content(schema = @Schema(implementation = Page.class)))
    @ApiResponse(responseCode = "404", description = "Комментарий не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/api/v1/comments/{commentId}/replies")
    public ResponseEntity<Page<CommentDTO>> getReplies(
        @Parameter(description = "Идентификатор комментария", required = true, example = "1")
        @PathVariable Long commentId,
        @Parameter(hidden = true) Pageable pageable
    ) {
        return ResponseEntity.ok(commentQueryPort.getReplies(commentId, pageable));
    }

    private Long resolveUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new AccessDeniedException("UNAUTHORIZED", "Пользователь не аутентифицирован");
        }
        try {
            return Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            throw new AccessDeniedException("INVALID_PRINCIPAL", "Некорректный идентификатор пользователя");
        }
    }
}
