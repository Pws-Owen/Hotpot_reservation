package org.li.hotpot_reservationsystem.dto;

import lombok.Data;
import java.util.List;

/**
 * 创建用户请求DTO
 */
@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private Integer userType; // 用户类型：1-系统用户，2-普通用户
    private List<Long> roleIds;
}

