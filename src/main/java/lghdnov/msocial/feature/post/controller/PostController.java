package lghdnov.msocial.feature.post.controller;

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
import lghdnov.msocial.feature.post.api.PostCommandPort;
import lghdnov.msocial.feature.post.api.PostQueryPort;
import lghdnov.msocial.feature.post.presentation.CreatePostRequest;
import lghdnov.msocial.feature.post.presentation.PostDTO;
import lghdnov.msocial.feature.post.presentation.UpdatePostRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Tag(name = "Posts", description = "Управление постами")
@SecurityRequirement(name = "bearerAuth")
@RestController
public class PostController {

    private final PostQueryPort postQueryPort;
    private final PostCommandPort postCommandPort;

    public PostController(PostQueryPort postQueryPort, PostCommandPort postCommandPort) {
        this.postQueryPort = postQueryPort;
        this.postCommandPort = postCommandPort;
    }

    @Operation(summary = "Получить пост по ID")
    @ApiResponse(responseCode = "200", description = "Пост найден",
        content = @Content(schema = @Schema(implementation = PostDTO.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пост не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/api/v1/posts/{postId}")
    public ResponseEntity<PostDTO> getPost(
        Principal principal,
        @Parameter(description = "Идентификатор поста", required = true, example = "1")
        @PathVariable Long postId
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(postQueryPort.getPost(userId, postId));
    }

    @Operation(summary = "Получить ленту постов пользователя")
    @ApiResponse(responseCode = "200", description = "Лента постов",
        content = @Content(schema = @Schema(implementation = Page.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/api/v1/users/{userId}/posts")
    public ResponseEntity<Page<PostDTO>> getFeed(
        @Parameter(description = "Идентификатор пользователя", required = true, example = "1")
        @PathVariable Long userId,
        @Parameter(hidden = true) Pageable pageable
    ) {
        return ResponseEntity.ok(postQueryPort.getFeed(userId, pageable));
    }

    @Operation(summary = "Создать пост")
    @ApiResponse(responseCode = "201", description = "Пост создан",
        content = @Content(schema = @Schema(implementation = PostDTO.class)))
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/api/v1/posts")
    public ResponseEntity<PostDTO> createPost(
        Principal principal,
        @Valid @RequestBody CreatePostRequest request
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(postCommandPort.createPost(userId, request));
    }

    @Operation(summary = "Обновить пост")
    @ApiResponse(responseCode = "200", description = "Пост обновлён",
        content = @Content(schema = @Schema(implementation = PostDTO.class)))
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Нет доступа",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пост не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/api/v1/posts/{postId}")
    public ResponseEntity<PostDTO> updatePost(
        Principal principal,
        @Parameter(description = "Идентификатор поста", required = true, example = "1")
        @PathVariable Long postId,
        @Valid @RequestBody UpdatePostRequest request
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(postCommandPort.updatePost(userId, postId, request));
    }

    @Operation(summary = "Удалить пост")
    @ApiResponse(responseCode = "204", description = "Пост удалён")
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Нет доступа",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пост не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/api/v1/posts/{postId}")
    public ResponseEntity<Void> deletePost(
        Principal principal,
        @Parameter(description = "Идентификатор поста", required = true, example = "1")
        @PathVariable Long postId
    ) {
        Long userId = resolveUserId(principal);
        postCommandPort.deletePost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Опубликовать пост")
    @ApiResponse(responseCode = "200", description = "Пост опубликован",
        content = @Content(schema = @Schema(implementation = PostDTO.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Нет доступа",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пост не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/api/v1/posts/{postId}/publish")
    public ResponseEntity<PostDTO> publishPost(
        Principal principal,
        @Parameter(description = "Идентификатор поста", required = true, example = "1")
        @PathVariable Long postId
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(postCommandPort.publishPost(userId, postId));
    }

    @Operation(summary = "Добавить медиафайлы к посту")
    @ApiResponse(responseCode = "200", description = "Медиа добавлены",
        content = @Content(schema = @Schema(implementation = PostDTO.class)))
    @ApiResponse(responseCode = "400", description = "Файлы некорректны",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Нет доступа",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пост не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/api/v1/posts/{postId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> addPostMedia(
        Principal principal,
        @Parameter(description = "Идентификатор поста", required = true, example = "1")
        @PathVariable Long postId,
        @Parameter(description = "Медиафайлы для загрузки", required = true)
        @RequestParam("files") List<MultipartFile> files
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(postCommandPort.addPostMedia(userId, postId, files));
    }

    @Operation(summary = "Удалить медиафайл из поста")
    @ApiResponse(responseCode = "204", description = "Медиа удалено")
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Нет доступа",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пост или медиа не найдены",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/api/v1/posts/{postId}/media/{mediaId}")
    public ResponseEntity<Void> deletePostMedia(
        Principal principal,
        @Parameter(description = "Идентификатор поста", required = true, example = "1")
        @PathVariable Long postId,
        @Parameter(description = "Идентификатор медиа", required = true, example = "1")
        @PathVariable Long mediaId
    ) {
        Long userId = resolveUserId(principal);
        postCommandPort.deletePostMedia(userId, postId, mediaId);
        return ResponseEntity.noContent().build();
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
