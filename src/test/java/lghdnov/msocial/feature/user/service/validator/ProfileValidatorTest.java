package lghdnov.msocial.feature.user.service.validator;

import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.user.presentation.ProfileUpdateRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class ProfileValidatorTest {

    private final ProfileValidator validator = new ProfileValidator();

    @Test
    void validate_shouldPass_whenBirthDateIsInPast() {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
            LocalDate.of(1990, 1, 1), null, null
        );
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    @Test
    void validate_shouldPass_whenBirthDateIsNull() {
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null);
        assertThatNoException().isThrownBy(() -> validator.validate(request));
    }

    @Test
    void validate_shouldThrow_whenBirthDateIsInFuture() {
        ProfileUpdateRequest request = new ProfileUpdateRequest(
            LocalDate.now().plusDays(1), null, null
        );
        assertThatThrownBy(() -> validator.validate(request))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Дата рождения не может быть в будущем");
    }
}
