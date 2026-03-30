package org.li.hotpot_reservationsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.MenuItem;

/**
 * 菜单Service接口
 */
public interface MenuItemService extends IService<MenuItem> {
    /**
     * 分页查询菜品列表
     */
    PageResult<MenuItem> getMenuItemPage(Long current, Long size, Long categoryId, Integer status, String itemName);
}

