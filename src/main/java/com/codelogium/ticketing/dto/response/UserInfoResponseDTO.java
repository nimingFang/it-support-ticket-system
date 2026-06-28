package com.codelogium.ticketing.dto.response;

import com.codelogium.ticketing.entity.enums.UserRole;
import lombok.Data;

@Data
public class UserInfoResponseDTO {

    private Long id;
    private String username;
    private String email;
    private UserRole role;
}
