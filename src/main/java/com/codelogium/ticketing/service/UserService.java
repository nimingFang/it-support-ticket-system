package com.codelogium.ticketing.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.codelogium.ticketing.dto.request.UserRegisterRequestDTO;
import com.codelogium.ticketing.dto.response.UserInfoResponseDTO;
import com.codelogium.ticketing.entity.User;
import com.codelogium.ticketing.entity.enums.UserRole;

public interface UserService {
    /** 注册用户 */
    UserInfoResponseDTO createUser(UserRegisterRequestDTO dto);

    /** 根据 ID 查询用户（对外接口，不含密码） */
    UserInfoResponseDTO retrieveUser(Long userId);

    /** 根据用户名查询（Spring Security 内部使用，含完整字段） */
    User retrieveUser(String username);

    /** 分页查询用户列表（支持 username 模糊 + role 精确筛选） */
    Page<UserInfoResponseDTO> listUsers(String username, String role, Pageable pageable);

    /** 修改用户角色 */
    void updateUserRole(Long userId, UserRole role);

    /** 删除用户（级联删除工单和评论） */
    void removeUser(Long userId);
}
