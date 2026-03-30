package org.li.hotpot_reservationsystem.dto;

import lombok.Data;
import java.util.List;

/**
 * 更新用户请求DTO
 */
@Data
public class UpdateUserRequest {
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
    private List<Long> roleIds;
}

