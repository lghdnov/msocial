package lghdnov.msocial.feature.post.presentation.mapper;

import lghdnov.msocial.feature.post.entity.Post;
import lghdnov.msocial.feature.post.entity.PostMedia;
import lghdnov.msocial.feature.post.presentation.PostDTO;
import lghdnov.msocial.feature.post.presentation.PostMediaDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PostMapper {

    @Mapping(target = "id", source = "post.id")
    @Mapping(target = "authorId", source = "post.authorId")
    @Mapping(target = "authorName", source = "post.authorName")
    @Mapping(target = "content", source = "post.content")
    @Mapping(target = "published", source = "post.published")
    @Mapping(target = "createdAt", source = "post.createdAt")
    @Mapping(target = "media", source = "media")
    PostDTO toDto(Post post, List<PostMedia> media);

    PostMediaDTO toDto(PostMedia postMedia);

    List<PostMediaDTO> toDtoList(List<PostMedia> media);
}
