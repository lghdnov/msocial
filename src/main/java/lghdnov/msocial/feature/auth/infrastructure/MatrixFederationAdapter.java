package lghdnov.msocial.feature.auth.infrastructure;

import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.auth.api.OidcVerificationPort;
import lghdnov.msocial.feature.auth.entity.MatrixUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Адаптер верификации OIDC-токенов через Matrix Federation API.
 *
 * <p>Реализует {@link OidcVerificationPort}, выполняя HTTP-вызов
 * {@code POST /_matrix/federation/v1/openid/userinfo}.
 */
@Component
class MatrixFederationAdapter implements OidcVerificationPort {

    private final RestClient restClient;

    MatrixFederationAdapter(
        @Value("${matrix.federation.base-url:}") String baseUrl,
        RestClient.Builder restClientBuilder
    ) {
        String url = baseUrl.isBlank() ? "https://matrix.org" : baseUrl;
        this.restClient = restClientBuilder
            .baseUrl(url)
            .build();
    }

    @Override
    public MatrixUserInfo verifyOpenIdToken(String openidToken) {
        if (openidToken == null || openidToken.isBlank()) {
            throw new ValidationException("OIDC_TOKEN_EMPTY", "OpenID токен не передан");
        }

        try {
            Map<String, Object> response = restClient.post()
                .uri("/_matrix/federation/v1/openid/userinfo")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("access_token", openidToken))
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<>() {});

            if (response == null || !response.containsKey("sub")) {
                throw new ValidationException("OIDC_INVALID_RESPONSE", "Некорректный ответ от Matrix Federation");
            }

            return new MatrixUserInfo(
                (String) response.get("sub"),
                (String) response.get("avatar_url"),
                (String) response.get("display_name")
            );
        } catch (org.springframework.web.client.RestClientException e) {
            throw new ValidationException("OIDC_VERIFICATION_FAILED", "Не удалось верифицировать токен: " + e.getMessage());
        }
    }
}
