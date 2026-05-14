package lghdnov.msocial.feature.user.service.validator;

import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.user.presentation.ProfileUpdateRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Валидатор персональных данных профиля.
 */
@Component
public class ProfileValidator {

    public void validate(ProfileUpdateRequest request) {
        if (request.birthDate() != null && request.birthDate().isAfter(LocalDate.now())) {
            throw new ValidationException("BIRTHDATE_FUTURE", "Дата рождения не может быть в будущем");
        }
    }
}
