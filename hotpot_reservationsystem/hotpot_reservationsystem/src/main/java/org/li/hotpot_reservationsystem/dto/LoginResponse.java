package org.li.hotpot_reservationsystem.dto;

import lombok.Data;
import java.util.List;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private List<String> roles;
    private List<String> permissions;
}

