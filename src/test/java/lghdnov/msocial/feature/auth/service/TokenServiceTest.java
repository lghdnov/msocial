package lghdnov.msocial.feature.auth.service;

import io.jsonwebtoken.Claims;
import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.auth.entity.JwtClaims;
import lghdnov.msocial.feature.auth.infrastructure.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private TokenService tokenService;

    @Test
    void getAccessTokenExpirationSeconds_shouldReturnValueFromProvider() {
        when(jwtProvider.getAccessTokenExpirationSeconds()).thenReturn(900L);

        assertThat(tokenService.getAccessTokenExpirationSeconds()).isEqualTo(900L);
    }

    @Test
    void validateToken_shouldReturnTrue_whenTokenValid() {
        when(jwtProvider.verifySignature("valid")).thenReturn(mock(Claims.class));
        assertThat(tokenService.validateToken("valid")).isTrue();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenInvalid() {
        when(jwtProvider.verifySignature("invalid")).thenReturn(null);
        assertThat(tokenService.validateToken("invalid")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenBlank() {
        assertThat(tokenService.validateToken(" ")).isFalse();
        assertThat(tokenService.validateToken(null)).isFalse();
    }

    @Test
    void extractClaims_shouldReturnClaims_whenTokenValid() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("1");
        when(claims.get("sessionId", Long.class)).thenReturn(10L);
        when(claims.get("roles", Set.class)).thenReturn(Set.of("ROLE_USER"));
        when(jwtProvider.verifySignature("valid")).thenReturn(claims);

        JwtClaims result = tokenService.extractClaims("valid");

        assertThat(result.sub()).isEqualTo("1");
        assertThat(result.sessionId()).isEqualTo(10L);
        assertThat(result.roles()).containsExactly("ROLE_USER");
    }

    @Test
    void extractClaims_shouldThrow_whenTokenInvalid() {
        when(jwtProvider.verifySignature("invalid")).thenReturn(null);

        assertThatThrownBy(() -> tokenService.extractClaims("invalid"))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Токен повреждён или истёк");
    }
}
