package lghdnov.msocial.feature.user.service;

import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.user.api.UserCommandPort;
import lghdnov.msocial.feature.user.api.UserProvisioningPort;
import lghdnov.msocial.feature.user.api.UserQueryPort;
import lghdnov.msocial.feature.user.entity.PersonalInfo;
import lghdnov.msocial.feature.user.entity.User;
import lghdnov.msocial.feature.user.presentation.ProfileUpdateRequest;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import lghdnov.msocial.feature.user.presentation.mapper.UserMapper;
import lghdnov.msocial.feature.user.repository.PersonalInfoRepository;
import lghdnov.msocial.feature.user.repository.UserRepository;
import lghdnov.msocial.feature.user.service.validator.ProfileValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис управления пользователями.
 *
 * <p>Реализует {@link UserQueryPort}, {@link UserCommandPort}, {@link UserProvisioningPort}.
 * Оркестрирует работу с JPA-сущностями {@code User} и {@code PersonalInfo}.
 */
@Service
class UserService implements UserQueryPort, UserCommandPort, UserProvisioningPort {

    private final UserRepository userRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final UserMapper userMapper;
    private final ProfileValidator profileValidator;

    UserService(
        UserRepository userRepository,
        PersonalInfoRepository personalInfoRepository,
        UserMapper userMapper,
        ProfileValidator profileValidator
    ) {
        this.userRepository = userRepository;
        this.personalInfoRepository = personalInfoRepository;
        this.userMapper = userMapper;
        this.profileValidator = profileValidator;
    }

    @Override
    @Transactional
    public Long findByIdOrCreate(String externalId) {
        if (externalId == null || externalId.isBlank()) {
            throw new ValidationException("EXTERNAL_ID_EMPTY", "Внешний идентификатор обязателен");
        }
        return userRepository.findByExternalId(externalId)
            .map(User::getId)
            .orElseGet(() -> createUserWithProfile(externalId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAccountActive(Long userId) {
        return userId != null && userRepository.existsById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Пользователь не найден"));
        PersonalInfo info = personalInfoRepository.findByUserId(userId)
            .orElseGet(() -> PersonalInfo.builder().userId(userId).build());
        return userMapper.toDto(user, info);
    }

    @Override
    @Transactional
    public UserDTO updateProfile(Long userId, ProfileUpdateRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("USER_NOT_FOUND", "Пользователь не найден");
        }

        profileValidator.validate(request);

        PersonalInfo info = personalInfoRepository.findByUserId(userId)
            .orElseGet(() -> PersonalInfo.builder().userId(userId).build());

        applyPartialChanges(info, request);
        PersonalInfo saved = personalInfoRepository.save(info);

        User user = userRepository.findById(userId).orElseThrow();
        return userMapper.toDto(user, saved);
    }

    private Long createUserWithProfile(String externalId) {
        User user = User.builder()
            .externalId(externalId)
            .build();
        User savedUser = userRepository.save(user);

        PersonalInfo info = PersonalInfo.builder()
            .userId(savedUser.getId())
            .build();
        personalInfoRepository.save(info);

        return savedUser.getId();
    }

    private void applyPartialChanges(PersonalInfo info, ProfileUpdateRequest request) {
        if (request.birthDate() != null) {
            info.setBirthDate(request.birthDate());
        }
        if (request.address() != null) {
            info.setAddress(request.address());
        }
        if (request.favoriteTrack() != null) {
            info.setFavoriteTrack(request.favoriteTrack());
        }
        if (request.status() != null) {
            info.setStatus(request.status());
        }
    }
}
