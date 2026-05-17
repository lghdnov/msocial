package lghdnov.msocial.feature.comment.presentation.mapper;

import lghdnov.msocial.feature.comment.entity.Comment;
import lghdnov.msocial.feature.comment.presentation.CommentDTO;
import lghdnov.msocial.feature.comment.presentation.CreateCommentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "postId", source = "postId")
    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "authorName", source = "authorName")
    @Mapping(target = "parentId", source = "parentId")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    CommentDTO toDto(Comment comment);

    List<CommentDTO> toDtoList(List<Comment> comments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "postId", source = "postId")
    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "authorName", source = "authorName")
    @Mapping(target = "parentId", source = "request.parentId")
    @Mapping(target = "content", source = "request.content")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Comment toEntity(CreateCommentRequest request, Long postId, Long authorId, String authorName);
}
