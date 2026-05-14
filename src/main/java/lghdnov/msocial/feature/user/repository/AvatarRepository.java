package lghdnov.msocial.feature.user.repository;

import lghdnov.msocial.feature.user.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    List<Avatar> findByUserIdOrderByUploadedAtDesc(Long userId);

    Optional<Avatar> findByUserIdAndActiveTrue(Long userId);
}
