package lghdnov.msocial.feature.user.service;

import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.user.api.MediaStoragePort;
import lghdnov.msocial.feature.user.entity.Avatar;
import lghdnov.msocial.feature.user.entity.PersonalInfo;
import lghdnov.msocial.feature.user.entity.User;
import lghdnov.msocial.feature.user.presentation.AvatarDTO;
import lghdnov.msocial.feature.user.presentation.ProfileUpdateRequest;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import lghdnov.msocial.feature.user.presentation.mapper.UserMapper;
import lghdnov.msocial.feature.user.repository.AvatarRepository;
import lghdnov.msocial.feature.user.repository.PersonalInfoRepository;
import lghdnov.msocial.feature.user.repository.UserRepository;
import lghdnov.msocial.feature.user.service.validator.ProfileValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;
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
    private AvatarRepository avatarRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProfileValidator profileValidator;

    @Mock
    private MediaStoragePort mediaStoragePort;

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
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
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
    void getDisplayName_shouldReturnExternalId_whenUserExists() {
        User user = User.builder().id(1L).externalId("@user:example.org").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String result = userService.getDisplayName(1L);

        assertThat(result).isEqualTo("@user:example.org");
    }

    @Test
    void getDisplayName_shouldReturnNull_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        String result = userService.getDisplayName(99L);

        assertThat(result).isNull();
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
            "New status"
        );
        User user = User.builder().id(userId).externalId("@user:example.org").build();
        PersonalInfo existing = PersonalInfo.builder().userId(userId).status("Old status").build();
        PersonalInfo saved = PersonalInfo.builder().userId(userId).status("New status").build();
        UserDTO dto = new UserDTO(userId, "@user:example.org", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(personalInfoRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(personalInfoRepository.save(existing)).thenReturn(saved);
        when(userMapper.toDto(user, saved)).thenReturn(dto);

        UserDTO result = userService.updateProfile(userId, request);

        assertThat(result).isEqualTo(dto);
        assertThat(existing.getStatus()).isEqualTo("New status");
        assertThat(existing.getAddress()).isEqualTo("г. Москва");
        verify(profileValidator).validate(request);
    }

    @Test
    void updateProfile_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(99L, new ProfileUpdateRequest(null, null, null)))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void getAvatarHistory_shouldReturnList() {
        Long userId = 1L;
        List<Avatar> avatars = List.of(
            Avatar.builder().id(1L).userId(userId).url("/avatars/1.jpg").build()
        );
        List<AvatarDTO> dtos = List.of(new AvatarDTO(1L, "/avatars/1.jpg", null, true));

        when(avatarRepository.findByUserIdOrderByUploadedAtDesc(userId)).thenReturn(avatars);
        when(userMapper.toDtoList(avatars)).thenReturn(dtos);

        List<AvatarDTO> result = userService.getAvatarHistory(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).url()).isEqualTo("/avatars/1.jpg");
    }

    @Test
    void uploadAvatar_shouldSaveAndActivateNewAvatar() {
        Long userId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", new byte[]{1, 2, 3});
        User user = User.builder().id(userId).externalId("@user:example.org").build();
        PersonalInfo info = PersonalInfo.builder().userId(userId).build();
        UserDTO dto = new UserDTO(userId, "@user:example.org", null, null);
        Avatar oldAvatar = Avatar.builder().id(1L).userId(userId).url("/avatars/old.jpg").active(true).build();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(mediaStoragePort.uploadAvatar(file)).thenReturn("/avatars/new.jpg");
        when(avatarRepository.findByUserIdAndActiveTrue(userId)).thenReturn(Optional.of(oldAvatar));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(personalInfoRepository.findByUserId(userId)).thenReturn(Optional.of(info));
        when(userMapper.toDto(user, info)).thenReturn(dto);

        UserDTO result = userService.uploadAvatar(userId, file);

        assertThat(result).isEqualTo(dto);
        assertThat(oldAvatar.getActive()).isFalse();
        verify(avatarRepository, times(2)).save(any(Avatar.class));
    }

    @Test
    void uploadTrack_shouldSaveUrlToPersonalInfo() {
        Long userId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "track.mp3", "audio/mpeg", new byte[]{1, 2, 3});
        User user = User.builder().id(userId).externalId("@user:example.org").build();
        PersonalInfo info = PersonalInfo.builder().userId(userId).build();
        UserDTO dto = new UserDTO(userId, "@user:example.org", null, null);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(mediaStoragePort.uploadTrack(file)).thenReturn("/tracks/new.mp3");
        when(personalInfoRepository.findByUserId(userId)).thenReturn(Optional.of(info));
        when(personalInfoRepository.save(info)).thenReturn(info);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user, info)).thenReturn(dto);

        UserDTO result = userService.uploadTrack(userId, file);

        assertThat(result).isEqualTo(dto);
        assertThat(info.getFavoriteTrackUrl()).isEqualTo("/tracks/new.mp3");
    }
}
