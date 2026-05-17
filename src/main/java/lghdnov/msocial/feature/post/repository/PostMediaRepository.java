package lghdnov.msocial.feature.post.repository;

import lghdnov.msocial.feature.post.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {

    List<PostMedia> findByPostIdOrderBySortOrderAsc(Long postId);

    List<PostMedia> findByPostIdInOrderBySortOrderAsc(List<Long> postIds);
}
