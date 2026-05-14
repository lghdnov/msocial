package lghdnov.msocial.feature.user.controller;

import lghdnov.msocial.feature.user.api.UserCommandPort;
import lghdnov.msocial.feature.user.api.UserQueryPort;
import lghdnov.msocial.feature.user.presentation.AvatarDTO;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc;
    private UserQueryPort userQueryPort;
    private UserCommandPort userCommandPort;

    @BeforeEach
    void setUp() {
        userQueryPort = Mockito.mock(UserQueryPort.class);
        userCommandPort = Mockito.mock(UserCommandPort.class);
        UserController controller = new UserController(userQueryPort, userCommandPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private RequestPostProcessor asUser(String userId) {
        return request -> {
            request.setUserPrincipal(() -> userId);
            return request;
        };
    }

    @Test
    void getProfile_shouldReturn200_whenAuthenticated() throws Exception {
        when(userQueryPort.getProfile(1L))
            .thenReturn(new UserDTO(1L, "@user:example.org", null, null));

        mockMvc.perform(get("/api/v1/users/profile")
                .with(asUser("1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.externalId").value("@user:example.org"));
    }

    @Test
    void updateProfile_shouldReturn200_whenValidRequest() throws Exception {
        when(userCommandPort.updateProfile(eq(1L), any()))
            .thenReturn(new UserDTO(1L, "@user:example.org", null, null));

        mockMvc.perform(put("/api/v1/users/profile")
                .with(asUser("1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"New status\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateProfile_shouldReturn400_whenStatusTooLong() throws Exception {
        String longStatus = "a".repeat(300);

        mockMvc.perform(put("/api/v1/users/profile")
                .with(asUser("1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + longStatus + "\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getAvatarHistory_shouldReturn200() throws Exception {
        when(userQueryPort.getAvatarHistory(1L))
            .thenReturn(List.of(new AvatarDTO(1L, "/avatars/1.jpg", Instant.now(), true)));

        mockMvc.perform(get("/api/v1/users/profile/avatars")
                .with(asUser("1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].url").value("/avatars/1.jpg"));
    }

    @Test
    void uploadAvatar_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(userCommandPort.uploadAvatar(eq(1L), any()))
            .thenReturn(new UserDTO(1L, "@user:example.org", null, null));

        mockMvc.perform(multipart("/api/v1/users/profile/avatar")
                .file(file)
                .with(asUser("1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void uploadTrack_shouldReturn200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "track.mp3", "audio/mpeg", new byte[]{1, 2, 3});

        when(userCommandPort.uploadTrack(eq(1L), any()))
            .thenReturn(new UserDTO(1L, "@user:example.org", null, null));

        mockMvc.perform(multipart("/api/v1/users/profile/track")
                .file(file)
                .with(asUser("1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
}
