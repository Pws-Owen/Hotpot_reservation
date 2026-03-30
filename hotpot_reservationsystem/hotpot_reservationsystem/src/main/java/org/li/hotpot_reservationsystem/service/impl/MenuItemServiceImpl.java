package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.entity.MenuItem;
import org.li.hotpot_reservationsystem.mapper.MenuItemMapper;
import org.li.hotpot_reservationsystem.service.MenuItemService;
import org.springframework.stereotype.Service;

/**
 * 菜单Service实现类
 */
@Service
public class MenuItemServiceImpl extends ServiceImpl<MenuItemMapper, MenuItem> implements MenuItemService {

    @Override
    public PageResult<MenuItem> getMenuItemPage(Long current, Long size, Long categoryId, Integer status, String itemName) {
        Page<MenuItem> page = new Page<>(current, size);
        LambdaQueryWrapper<MenuItem> wrapper = new QueryWrapper<MenuItem>().lambda();
        
        if (categoryId != null) {
            wrapper.eq(MenuItem::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(MenuItem::getStatus, status);
        }
        if (itemName != null && !itemName.trim().isEmpty()) {
            wrapper.like(MenuItem::getItemName, itemName.trim());
        }
        
        wrapper.orderByAsc(MenuItem::getSortOrder);
        Page<MenuItem> result = this.page(page, wrapper);
        
        return PageResult.of(result);
    }
}

