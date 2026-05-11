package lghdnov.msocial.feature.auth.controller;

import lghdnov.msocial.feature.auth.api.AuthCommandPort;
import lghdnov.msocial.feature.auth.api.TokenValidationPort;
import lghdnov.msocial.feature.auth.presentation.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthCommandPort authCommandPort;
    private TokenValidationPort tokenValidationPort;

    @BeforeEach
    void setUp() {
        authCommandPort = Mockito.mock(AuthCommandPort.class);
        tokenValidationPort = Mockito.mock(TokenValidationPort.class);
        AuthController controller = new AuthController(authCommandPort, tokenValidationPort);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void login_shouldReturn200_whenValidRequest() throws Exception {
        when(authCommandPort.login(any()))
            .thenReturn(new AuthResponse("access_jwt", "refresh_token", 900));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"openidToken\":\"valid_token\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access_jwt"))
            .andExpect(jsonPath("$.refreshToken").value("refresh_token"))
            .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void login_shouldReturn400_whenTokenMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"openidToken\":\" \"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_shouldReturn200_whenValidRequest() throws Exception {
        when(authCommandPort.refresh(any()))
            .thenReturn(new AuthResponse("new_access", "new_refresh", 900));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"valid_refresh\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("new_access"));
    }

    @Test
    void logout_shouldReturn204_whenValidToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("X-Refresh-Token", "valid_refresh"))
            .andExpect(status().isNoContent());
    }
}
