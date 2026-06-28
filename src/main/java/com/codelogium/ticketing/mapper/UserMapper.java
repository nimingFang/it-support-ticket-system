package com.codelogium.ticketing.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codelogium.ticketing.dto.request.UserRegisterRequestDTO;
import com.codelogium.ticketing.dto.response.UserInfoResponseDTO;
import com.codelogium.ticketing.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /** 注册请求 DTO → User 实体 */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "EMPLOYEE")
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "comments", ignore = true)
    User toEntity(UserRegisterRequestDTO dto);

    /** User 实体 → 用户信息响应 DTO（不含 password） */
    UserInfoResponseDTO toResponseDTO(User user);
}
