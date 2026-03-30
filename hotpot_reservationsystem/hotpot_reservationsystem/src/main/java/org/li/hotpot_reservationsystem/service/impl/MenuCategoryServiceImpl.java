package org.li.hotpot_reservationsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.li.hotpot_reservationsystem.entity.MenuCategory;
import org.li.hotpot_reservationsystem.mapper.MenuCategoryMapper;
import org.li.hotpot_reservationsystem.service.MenuCategoryService;
import org.springframework.stereotype.Service;

/**
 * 菜单分类Service实现类
 */
@Service
public class MenuCategoryServiceImpl extends ServiceImpl<MenuCategoryMapper, MenuCategory> implements MenuCategoryService {
}

