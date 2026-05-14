package lghdnov.msocial.feature.user.presentation.mapper;

import lghdnov.msocial.feature.user.entity.PersonalInfo;
import lghdnov.msocial.feature.user.entity.User;
import lghdnov.msocial.feature.user.presentation.PersonalInfoDTO;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "externalId", source = "user.externalId")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "personalInfo", source = "personalInfo")
    UserDTO toDto(User user, PersonalInfo personalInfo);

    PersonalInfoDTO toDto(PersonalInfo personalInfo);
}
