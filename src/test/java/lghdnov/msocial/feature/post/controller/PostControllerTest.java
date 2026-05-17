package lghdnov.msocial.feature.post.controller;

import lghdnov.msocial.common.exceptions.GlobalExceptionHandler;
import lghdnov.msocial.feature.post.api.PostCommandPort;
import lghdnov.msocial.feature.post.api.PostQueryPort;
import lghdnov.msocial.feature.post.presentation.PostDTO;
import lghdnov.msocial.feature.post.presentation.PostMediaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerTest {

    private MockMvc mockMvc;
    private PostQueryPort postQueryPort;
    private PostCommandPort postCommandPort;

    @BeforeEach
    void setUp() {
        postQueryPort = Mockito.mock(PostQueryPort.class);
        postCommandPort = Mockito.mock(PostCommandPort.class);
        PostController controller = new PostController(postQueryPort, postCommandPort);
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
    void getPost_shouldReturn200() throws Exception {
        when(postQueryPort.getPost(1L, 1L))
            .thenReturn(new PostDTO(1L, 1L, "@user:example.org", "Hello", List.of(), true, Instant.now()));

        mockMvc.perform(get("/api/v1/posts/1")
                .with(asUser("1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    void getFeed_shouldReturn200() throws Exception {
        PostDTO dto = new PostDTO(1L, 1L, "@user:example.org", "Hello", List.of(), true, Instant.now());
        when(postQueryPort.getFeed(eq(1L), any()))
            .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/users/1/posts")
                .with(asUser("1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void createPost_shouldReturn201() throws Exception {
        when(postCommandPort.createPost(eq(1L), any()))
            .thenReturn(new PostDTO(1L, 1L, "@user:example.org", "New post", List.of(), false, Instant.now()));

        mockMvc.perform(post("/api/v1/posts")
                .with(asUser("1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"New post\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updatePost_shouldReturn200() throws Exception {
        when(postCommandPort.updatePost(eq(1L), eq(1L), any()))
            .thenReturn(new PostDTO(1L, 1L, "@user:example.org", "Updated", List.of(), true, Instant.now()));

        mockMvc.perform(put("/api/v1/posts/1")
                .with(asUser("1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Updated\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Updated"));
    }

    @Test
    void deletePost_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1")
                .with(asUser("1")))
            .andExpect(status().isNoContent());
    }

    @Test
    void publishPost_shouldReturn200() throws Exception {
        when(postCommandPort.publishPost(1L, 1L))
            .thenReturn(new PostDTO(1L, 1L, "@user:example.org", "Published", List.of(), true, Instant.now()));

        mockMvc.perform(post("/api/v1/posts/1/publish")
                .with(asUser("1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    void addPostMedia_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "image.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(postCommandPort.addPostMedia(eq(1L), eq(1L), any()))
            .thenReturn(new PostDTO(1L, 1L, "@user:example.org", "Content", List.of(new PostMediaDTO(1L, "/post-media/uuid.jpg", 0)), true, Instant.now()));

        mockMvc.perform(multipart("/api/v1/posts/1/media")
                .file(file)
                .with(asUser("1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.media[0].url").value("/post-media/uuid.jpg"));
    }

    @Test
    void deletePostMedia_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1/media/2")
                .with(asUser("1")))
            .andExpect(status().isNoContent());
    }

    @Test
    void createPost_shouldReturn403_whenPrincipalIsNull() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"New post\"}"))
            .andExpect(status().isForbidden());
    }
}
