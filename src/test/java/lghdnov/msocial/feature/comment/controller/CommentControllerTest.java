package lghdnov.msocial.feature.comment.controller;

import lghdnov.msocial.common.exceptions.GlobalExceptionHandler;
import lghdnov.msocial.feature.comment.api.CommentCommandPort;
import lghdnov.msocial.feature.comment.api.CommentQueryPort;
import lghdnov.msocial.feature.comment.entity.CommentStatus;
import lghdnov.msocial.feature.comment.presentation.CommentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentControllerTest {

    private MockMvc mockMvc;
    private CommentQueryPort commentQueryPort;
    private CommentCommandPort commentCommandPort;

    @BeforeEach
    void setUp() {
        commentQueryPort = Mockito.mock(CommentQueryPort.class);
        commentCommandPort = Mockito.mock(CommentCommandPort.class);
        CommentController controller = new CommentController(commentQueryPort, commentCommandPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    private RequestPostProcessor asUser(String userId) {
        return request -> {
            request.setUserPrincipal(() -> userId);
            return request;
        };
    }

    @Test
    void getComments_shouldReturn200() throws Exception {
        CommentDTO dto = new CommentDTO(1L, 1L, 1L, "@user:example.org", null, "Hello", CommentStatus.PUBLISHED, Instant.now(), null);
        when(commentQueryPort.getByPostId(eq(1L), any()))
            .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/posts/1/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].content").value("Hello"));
    }

    @Test
    void createComment_shouldReturn201() throws Exception {
        when(commentCommandPort.create(eq(1L), eq(1L), any()))
            .thenReturn(new CommentDTO(1L, 1L, 1L, "@user:example.org", null, "Nice!", CommentStatus.PUBLISHED, Instant.now(), null));

        mockMvc.perform(post("/api/v1/posts/1/comments")
                .with(asUser("1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Nice!\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.content").value("Nice!"));
    }

    @Test
    void getComment_shouldReturn200() throws Exception {
        when(commentQueryPort.getById(1L))
            .thenReturn(new CommentDTO(1L, 1L, 1L, "@user:example.org", null, "Hello", CommentStatus.PUBLISHED, Instant.now(), null));

        mockMvc.perform(get("/api/v1/comments/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateComment_shouldReturn200() throws Exception {
        when(commentCommandPort.update(eq(1L), eq(1L), any()))
            .thenReturn(new CommentDTO(1L, 1L, 1L, "@user:example.org", null, "Updated", CommentStatus.PUBLISHED, Instant.now(), Instant.now()));

        mockMvc.perform(put("/api/v1/comments/1")
                .with(asUser("1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Updated\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Updated"));
    }

    @Test
    void deleteComment_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/comments/1")
                .with(asUser("1")))
            .andExpect(status().isNoContent());
    }

    @Test
    void getReplies_shouldReturn200() throws Exception {
        CommentDTO dto = new CommentDTO(2L, 1L, 2L, "@reply:example.org", 1L, "Reply", CommentStatus.PUBLISHED, Instant.now(), null);
        when(commentQueryPort.getReplies(eq(1L), any()))
            .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/comments/1/replies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].parentId").value(1));
    }

    @Test
    void createComment_shouldReturn403_whenPrincipalIsNull() throws Exception {
        mockMvc.perform(post("/api/v1/posts/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Nice!\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void createComment_shouldReturn400_whenContentTooLong() throws Exception {
        String longContent = "a".repeat(2001);
        mockMvc.perform(post("/api/v1/posts/1/comments")
                .with(asUser("1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"" + longContent + "\"}"))
            .andExpect(status().isBadRequest());
    }
}
