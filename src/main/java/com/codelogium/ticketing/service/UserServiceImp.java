package com.codelogium.ticketing.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.codelogium.ticketing.dto.request.UserRegisterRequestDTO;
import com.codelogium.ticketing.dto.response.UserInfoResponseDTO;
import com.codelogium.ticketing.entity.User;
import com.codelogium.ticketing.entity.enums.UserRole;
import com.codelogium.ticketing.exception.ResourceNotFoundException;
import com.codelogium.ticketing.mapper.UserMapper;
import com.codelogium.ticketing.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImp implements UserService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserMapper userMapper;

    @Override
    public UserInfoResponseDTO createUser(UserRegisterRequestDTO dto) {
        // 注册防重：校验用户名是否已存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + dto.getUsername());
        }
        // 注册防重：校验邮箱是否已存在
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("邮箱已被注册: " + dto.getEmail());
        }
        // MapStruct: DTO → Entity
        User user = userMapper.toEntity(dto);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        // MapStruct: Entity → ResponseDTO（不含密码）
        return userMapper.toResponseDTO(savedUser);
    }

    @Cacheable(value = "users", key = "#userId")
    @Override
    public UserInfoResponseDTO retrieveUser(Long userId) {
        User user = unwrapUser(userId, userRepository.findById(userId));
        return userMapper.toResponseDTO(user);
    }

    /** Spring Security 使用：返回完整 User 实体 */
    @Override
    public User retrieveUser(String username) {
        return unwrapUser(404L, userRepository.findByUsername(username));
    }

    @Override
    public Page<UserInfoResponseDTO> listUsers(String username, String role, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (username != null && !username.isBlank()) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            if (role != null && !role.isBlank()) {
                predicates.add(cb.equal(root.get("role"), UserRole.valueOf(role)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, pageable).map(userMapper::toResponseDTO);
    }

    @CacheEvict(value = "users", key = "#userId")
    @Override
    public void updateUserRole(Long userId, UserRole role) {
        User user = unwrapUser(userId, userRepository.findById(userId));
        user.setRole(role);
        userRepository.save(user);
    }

    @CacheEvict(value = "users", key = "#userId")
    @Override
    public void removeUser(Long userId) {
        User user = unwrapUser(userId, userRepository.findById(userId));
        // 利用 cascade = ALL + orphanRemoval，删除 User 会自动级联删除其工单和评论
        userRepository.delete(user);
    }

    public void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId))
            throw new ResourceNotFoundException(userId, User.class);
    }

    public static User unwrapUser(Long userId, Optional<User> optionalUser) {
        return optionalUser.orElseThrow(() -> new ResourceNotFoundException(userId, User.class));
    }
}
