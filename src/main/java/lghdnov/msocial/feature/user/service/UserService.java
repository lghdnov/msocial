package lghdnov.msocial.feature.user.service;

import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.user.api.MediaStoragePort;
import lghdnov.msocial.feature.user.api.UserCommandPort;
import lghdnov.msocial.feature.user.api.UserProvisioningPort;
import lghdnov.msocial.feature.user.api.UserQueryPort;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Сервис управления пользователями.
 *
 * <p>Реализует {@link UserQueryPort}, {@link UserCommandPort}, {@link UserProvisioningPort}.
 * Оркестрирует работу с JPA-сущностями {@code User}, {@code PersonalInfo}, {@code Avatar}
 * и медиа-хранилищем через {@link MediaStoragePort}.
 */
@Service
class UserService implements UserQueryPort, UserCommandPort, UserProvisioningPort {

    private final UserRepository userRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final AvatarRepository avatarRepository;
    private final UserMapper userMapper;
    private final ProfileValidator profileValidator;
    private final MediaStoragePort mediaStoragePort;

    UserService(
        UserRepository userRepository,
        PersonalInfoRepository personalInfoRepository,
        AvatarRepository avatarRepository,
        UserMapper userMapper,
        ProfileValidator profileValidator,
        MediaStoragePort mediaStoragePort
    ) {
        this.userRepository = userRepository;
        this.personalInfoRepository = personalInfoRepository;
        this.avatarRepository = avatarRepository;
        this.userMapper = userMapper;
        this.profileValidator = profileValidator;
        this.mediaStoragePort = mediaStoragePort;
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
        return userId != null && userRepository.findById(userId).isPresent();
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
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Пользователь не найден"));

        profileValidator.validate(request);

        PersonalInfo info = personalInfoRepository.findByUserId(userId)
            .orElseGet(() -> PersonalInfo.builder().userId(userId).build());

        applyPartialChanges(info, request);
        PersonalInfo saved = personalInfoRepository.save(info);

        return userMapper.toDto(user, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvatarDTO> getAvatarHistory(Long userId) {
        return userMapper.toDtoList(avatarRepository.findByUserIdOrderByUploadedAtDesc(userId));
    }

    @Override
    @Transactional
    public UserDTO uploadAvatar(Long userId, MultipartFile file) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("USER_NOT_FOUND", "Пользователь не найден");
        }

        String url = mediaStoragePort.uploadAvatar(file);
        registerRollbackCleanup(url);

        avatarRepository.findByUserIdAndActiveTrue(userId)
            .ifPresent(old -> {
                old.setActive(false);
                avatarRepository.save(old);
            });

        Avatar avatar = Avatar.builder()
            .userId(userId)
            .url(url)
            .build();
        avatarRepository.save(avatar);

        return getProfile(userId);
    }

    @Override
    @Transactional
    public UserDTO uploadTrack(Long userId, MultipartFile file) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("USER_NOT_FOUND", "Пользователь не найден");
        }

        String url = mediaStoragePort.uploadTrack(file);

        PersonalInfo info = personalInfoRepository.findByUserId(userId)
            .orElseGet(() -> PersonalInfo.builder().userId(userId).build());

        info.setFavoriteTrackUrl(url);
        personalInfoRepository.save(info);

        User user = userRepository.findById(userId).orElseThrow();
        return userMapper.toDto(user, info);
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

    private void registerRollbackCleanup(String url) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        mediaStoragePort.delete(url);
                    }
                }
            });
        }
    }

    private void applyPartialChanges(PersonalInfo info, ProfileUpdateRequest request) {
        if (request.birthDate() != null) {
            info.setBirthDate(request.birthDate());
        }
        if (request.address() != null) {
            info.setAddress(request.address());
        }
        if (request.status() != null) {
            info.setStatus(request.status());
        }
    }
}
