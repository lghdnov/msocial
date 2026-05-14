package lghdnov.msocial.feature.user.repository;

import lghdnov.msocial.feature.user.entity.PersonalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalInfoRepository extends JpaRepository<PersonalInfo, Long> {

    Optional<PersonalInfo> findByUserId(Long userId);
}
