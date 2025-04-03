package com.chuong.training.mapper;

import com.chuong.training.dto.request.UserCreationRequest;
import com.chuong.training.dto.request.UserUpdateRequest;
import com.chuong.training.dto.response.UserResponse;
import com.chuong.training.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}