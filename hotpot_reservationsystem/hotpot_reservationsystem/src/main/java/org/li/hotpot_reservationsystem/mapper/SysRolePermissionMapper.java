package org.li.hotpot_reservationsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.li.hotpot_reservationsystem.entity.SysRolePermission;

/**
 * 角色权限关联Mapper接口
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}

