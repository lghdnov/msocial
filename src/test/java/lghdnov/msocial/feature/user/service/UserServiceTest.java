package lghdnov.msocial.feature.user.service;

import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.user.entity.PersonalInfo;
import lghdnov.msocial.feature.user.entity.User;
import lghdnov.msocial.feature.user.presentation.ProfileUpdateRequest;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import lghdnov.msocial.feature.user.presentation.mapper.UserMapper;
import lghdnov.msocial.feature.user.repository.PersonalInfoRepository;
import lghdnov.msocial.feature.user.repository.UserRepository;
import lghdnov.msocial.feature.user.service.validator.ProfileValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PersonalInfoRepository personalInfoRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProfileValidator profileValidator;

    @InjectMocks
    private UserService userService;

    @Test
    void findByIdOrCreate_shouldReturnExistingId_whenUserExists() {
        String externalId = "@user:example.org";
        User user = User.builder().id(1L).externalId(externalId).build();

        when(userRepository.findByExternalId(externalId)).thenReturn(Optional.of(user));

        Long result = userService.findByIdOrCreate(externalId);

        assertThat(result).isEqualTo(1L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByIdOrCreate_shouldCreateUser_whenUserNotExists() {
        String externalId = "@newuser:example.org";
        User savedUser = User.builder().id(2L).externalId(externalId).build();

        when(userRepository.findByExternalId(externalId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(personalInfoRepository.save(any(PersonalInfo.class))).thenAnswer(inv -> inv.getArgument(0));

        Long result = userService.findByIdOrCreate(externalId);

        assertThat(result).isEqualTo(2L);
        verify(userRepository).save(any(User.class));
        verify(personalInfoRepository).save(any(PersonalInfo.class));
    }

    @Test
    void findByIdOrCreate_shouldThrow_whenExternalIdIsBlank() {
        assertThatThrownBy(() -> userService.findByIdOrCreate(" "))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Внешний идентификатор обязателен");
    }

    @Test
    void isAccountActive_shouldReturnTrue_whenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        assertThat(userService.isAccountActive(1L)).isTrue();
    }

    @Test
    void isAccountActive_shouldReturnFalse_whenUserIdIsNull() {
        assertThat(userService.isAccountActive(null)).isFalse();
    }

    @Test
    void getProfile_shouldReturnDto_whenUserExists() {
        Long userId = 1L;
        User user = User.builder().id(userId).externalId("@user:example.org").build();
        PersonalInfo info = PersonalInfo.builder().userId(userId).status("Hello").build();
        UserDTO dto = new UserDTO(userId, "@user:example.org", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(personalInfoRepository.findByUserId(userId)).thenReturn(Optional.of(info));
        when(userMapper.toDto(user, info)).thenReturn(dto);

        UserDTO result = userService.getProfile(userId);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getProfile_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void updateProfile_shouldUpdateAndReturnDto() {
        Long userId = 1L;
        ProfileUpdateRequest request = new ProfileUpdateRequest(
            LocalDate.of(1990, 5, 20),
            "г. Москва",
            "The Beatles - Yesterday",
            "New status"
        );
        User user = User.builder().id(userId).externalId("@user:example.org").build();
        PersonalInfo existing = PersonalInfo.builder().userId(userId).status("Old status").build();
        PersonalInfo saved = PersonalInfo.builder().userId(userId).status("New status").build();
        UserDTO dto = new UserDTO(userId, "@user:example.org", null, null);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(personalInfoRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(personalInfoRepository.save(existing)).thenReturn(saved);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user, saved)).thenReturn(dto);

        UserDTO result = userService.updateProfile(userId, request);

        assertThat(result).isEqualTo(dto);
        assertThat(existing.getStatus()).isEqualTo("New status");
        assertThat(existing.getAddress()).isEqualTo("г. Москва");
        verify(profileValidator).validate(request);
    }

    @Test
    void updateProfile_shouldThrow_whenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.updateProfile(99L, new ProfileUpdateRequest(null, null, null, null)))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Пользователь не найден");
    }
}
