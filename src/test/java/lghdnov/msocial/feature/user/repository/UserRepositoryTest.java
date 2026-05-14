package lghdnov.msocial.feature.user.repository;

import lghdnov.msocial.TestcontainersConfiguration;
import lghdnov.msocial.feature.user.entity.Avatar;
import lghdnov.msocial.feature.user.entity.PersonalInfo;
import lghdnov.msocial.feature.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonalInfoRepository personalInfoRepository;

    @Autowired
    private AvatarRepository avatarRepository;

    @Test
    void findByExternalId_shouldReturnUser_whenExists() {
        User user = User.builder().externalId("@test:example.org").build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByExternalId("@test:example.org");

        assertThat(found).isPresent();
        assertThat(found.get().getExternalId()).isEqualTo("@test:example.org");
    }

    @Test
    void findByExternalId_shouldReturnEmpty_whenNotExists() {
        Optional<User> found = userRepository.findByExternalId("@missing:example.org");
        assertThat(found).isEmpty();
    }

    @Test
    void existsById_shouldReturnTrue_forSavedUser() {
        User user = User.builder().externalId("@exists:example.org").build();
        User saved = userRepository.save(user);

        assertThat(userRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    void personalInfo_shouldLinkToUser() {
        User user = User.builder().externalId("@profile:example.org").build();
        User saved = userRepository.save(user);

        PersonalInfo info = PersonalInfo.builder()
            .userId(saved.getId())
            .status("Hello world")
            .build();
        personalInfoRepository.save(info);

        Optional<PersonalInfo> found = personalInfoRepository.findByUserId(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo("Hello world");
    }

    @Test
    void avatar_shouldLinkToUser_andSupportActiveFilter() {
        User user = User.builder().externalId("@avatar:example.org").build();
        User saved = userRepository.save(user);

        Avatar oldAvatar = Avatar.builder()
            .userId(saved.getId())
            .url("/avatars/old.jpg")
            .active(false)
            .build();
        avatarRepository.save(oldAvatar);

        Avatar newAvatar = Avatar.builder()
            .userId(saved.getId())
            .url("/avatars/new.jpg")
            .active(true)
            .build();
        avatarRepository.save(newAvatar);

        List<Avatar> history = avatarRepository.findByUserIdOrderByUploadedAtDesc(saved.getId());
        Optional<Avatar> active = avatarRepository.findByUserIdAndActiveTrue(saved.getId());

        assertThat(history).hasSize(2);
        assertThat(active).isPresent();
        assertThat(active.get().getUrl()).isEqualTo("/avatars/new.jpg");
    }
}
