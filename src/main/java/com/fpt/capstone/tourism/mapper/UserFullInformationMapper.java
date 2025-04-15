package com.fpt.capstone.tourism.mapper;

import com.fpt.capstone.tourism.dto.request.SaleCreateUserRequestDTO;
import com.fpt.capstone.tourism.dto.request.UserCreationRequestDTO;
import com.fpt.capstone.tourism.dto.response.UserFullInformationResponseDTO;
import com.fpt.capstone.tourism.dto.response.UserResponseDTO;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserFullInformationMapper extends EntityMapper<UserFullInformationResponseDTO, User> {
    @Mapping(target = "roleNames", expression = "java(mapRoles(user.getUserRoles()))")
    @Mapping(target = "deleted", source = "deleted")
    UserFullInformationResponseDTO toDTO(User user);


    User toUser(SaleCreateUserRequestDTO dto);


    User toEntity(UserCreationRequestDTO userDTO);

    //Map UserRoles to a List of role names
    default List<String> mapRoles(Set<UserRole> userRoles) {
        if (userRoles == null) {
            return Collections.emptyList();
        }
        return userRoles.stream()
                .map(userRole -> userRole.getRole().getRoleName())
                .collect(Collectors.toList());
    }

    UserResponseDTO toResponseDTO(User user);
}
