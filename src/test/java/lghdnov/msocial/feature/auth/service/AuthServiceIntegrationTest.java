package lghdnov.msocial.feature.auth.service;

import lghdnov.msocial.TestcontainersConfiguration;
import lghdnov.msocial.feature.auth.api.AuthCommandPort;
import lghdnov.msocial.feature.auth.presentation.AuthResponse;
import lghdnov.msocial.feature.auth.presentation.LoginRequest;
import lghdnov.msocial.feature.user.api.UserProvisioningPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "auth.dev.skip-verify=true")
class AuthServiceIntegrationTest {

    @Autowired
    private AuthCommandPort authCommandPort;

    @Autowired
    private UserProvisioningPort userProvisioningPort;

    @Test
    void login_shouldWork_withDevSkipVerify() {
        LoginRequest request = new LoginRequest("any_token", "@devuser:example.org");

        AuthResponse response = authCommandPort.login(request);

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();

        Long userId = userProvisioningPort.findByIdOrCreate("@devuser:example.org");
        assertThat(userProvisioningPort.isAccountActive(userId)).isTrue();
    }
}
