package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.annotation.LogOperation;
import org.li.hotpot_reservationsystem.common.PageResult;
import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.entity.MenuCategory;
import org.li.hotpot_reservationsystem.entity.MenuItem;
import org.li.hotpot_reservationsystem.service.MenuCategoryService;
import org.li.hotpot_reservationsystem.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单Controller
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private MenuCategoryService menuCategoryService;

    @Autowired
    private MenuItemService menuItemService;

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result<List<MenuCategory>> getCategories() {
        try {
            List<MenuCategory> categories = menuCategoryService.list();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询菜品
     */
    @GetMapping("/items/page")
    public Result<PageResult<MenuItem>> getMenuItemPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String itemName) {
        try {
            PageResult<MenuItem> result = menuItemService.getMenuItemPage(current, size, categoryId, status, itemName);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取菜品详情
     */
    @GetMapping("/items/{id}")
    public Result<MenuItem> getMenuItemById(@PathVariable Long id) {
        try {
            MenuItem item = menuItemService.getById(id);
            if (item == null) {
                return Result.error("菜品不存在");
            }
            return Result.success(item);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 分类管理接口 ====================

    /**
     * 获取分类详情
     */
    @GetMapping("/categories/{id}")
    public Result<MenuCategory> getCategoryById(@PathVariable Long id) {
        try {
            MenuCategory category = menuCategoryService.getById(id);
            if (category == null) {
                return Result.error("分类不存在");
            }
            return Result.success(category);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建分类
     */
    @PostMapping("/categories")
    @LogOperation(operation = "新增菜单分类")
    public Result<MenuCategory> createCategory(@RequestBody MenuCategory category) {
        try {
            // 检查分类名称是否已存在
            MenuCategory existing = menuCategoryService.lambdaQuery()
                    .eq(MenuCategory::getCategoryName, category.getCategoryName())
                    .one();
            if (existing != null) {
                return Result.error("分类名称已存在");
            }
            
            // 如果没有提供分类编码，自动生成
            if (category.getCategoryCode() == null || category.getCategoryCode().trim().isEmpty()) {
                // 使用时间戳 + 分类名称的简单编码生成唯一编码
                String code = "CAT_" + System.currentTimeMillis() + "_" + 
                             category.getCategoryName().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                // 如果编码太长，截取前50个字符（数据库字段限制为50）
                if (code.length() > 50) {
                    code = code.substring(0, 50);
                }
                category.setCategoryCode(code);
            } else {
                // 如果提供了编码，检查是否已存在
                MenuCategory codeExisting = menuCategoryService.lambdaQuery()
                        .eq(MenuCategory::getCategoryCode, category.getCategoryCode())
                        .one();
                if (codeExisting != null) {
                    return Result.error("分类编码已存在");
                }
            }
            
            // 设置默认父分类ID
            if (category.getParentId() == null) {
                category.setParentId(0L);
            }
            
            // 设置默认状态为启用
            if (category.getStatus() == null) {
                category.setStatus(1); // 1-启用
            }
            
            // 设置默认排序
            if (category.getSortOrder() == null) {
                category.setSortOrder(0);
            }
            
            menuCategoryService.save(category);
            return Result.success(category);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新分类
     */
    @PutMapping("/categories/{id}")
    @LogOperation(operation = "编辑菜单分类")
    public Result<MenuCategory> updateCategory(@PathVariable Long id, @RequestBody MenuCategory category) {
        try {
            MenuCategory existing = menuCategoryService.getById(id);
            if (existing == null) {
                return Result.error("分类不存在");
            }
            
            // 如果修改了分类名称，检查新名称是否已存在
            if (!existing.getCategoryName().equals(category.getCategoryName())) {
                MenuCategory duplicate = menuCategoryService.lambdaQuery()
                        .eq(MenuCategory::getCategoryName, category.getCategoryName())
                        .ne(MenuCategory::getCategoryId, id)
                        .one();
                if (duplicate != null) {
                    return Result.error("分类名称已存在");
                }
            }
            
            // 如果修改了分类编码，检查新编码是否已存在
            if (category.getCategoryCode() != null && !category.getCategoryCode().equals(existing.getCategoryCode())) {
                MenuCategory codeDuplicate = menuCategoryService.lambdaQuery()
                        .eq(MenuCategory::getCategoryCode, category.getCategoryCode())
                        .ne(MenuCategory::getCategoryId, id)
                        .one();
                if (codeDuplicate != null) {
                    return Result.error("分类编码已存在");
                }
            } else if (category.getCategoryCode() == null || category.getCategoryCode().trim().isEmpty()) {
                // 如果没有提供编码，保持原有编码
                category.setCategoryCode(existing.getCategoryCode());
            }
            
            category.setCategoryId(id);
            menuCategoryService.updateById(category);
            return Result.success(category);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/categories/{id}")
    @LogOperation(operation = "删除菜单分类")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        try {
            MenuCategory category = menuCategoryService.getById(id);
            if (category == null) {
                return Result.error("分类不存在");
            }
            
            // 检查是否有菜品使用此分类
            long itemCount = menuItemService.lambdaQuery()
                    .eq(MenuItem::getCategoryId, id)
                    .count();
            if (itemCount > 0) {
                return Result.error("该分类下还有菜品，无法删除");
            }
            
            menuCategoryService.removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 菜品管理接口 ====================

    /**
     * 创建菜品
     */
    @PostMapping("/items")
    @LogOperation(operation = "新增菜品")
    public Result<MenuItem> createMenuItem(@RequestBody MenuItem item) {
        try {
            // 检查分类是否存在
            if (item.getCategoryId() != null) {
                MenuCategory category = menuCategoryService.getById(item.getCategoryId());
                if (category == null) {
                    return Result.error("分类不存在");
                }
            }
            
            // 自动生成菜品编码（用于数据库唯一性，但不显示在前端）
            if (item.getItemCode() == null || item.getItemCode().trim().isEmpty()) {
                // 使用时间戳 + 菜品名称的简单编码生成唯一编码
                String code = "ITEM_" + System.currentTimeMillis() + "_" + 
                             item.getItemName().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                // 如果编码太长，截取前50个字符（数据库字段限制为50）
                if (code.length() > 50) {
                    code = code.substring(0, 50);
                }
                item.setItemCode(code);
            }
            
            // 设置默认值
            if (item.getStatus() == null) {
                item.setStatus(1); // 1-上架
            }
            if (item.getStock() == null) {
                item.setStock(0);
            }
            if (item.getSalesCount() == null) {
                item.setSalesCount(0);
            }
            if (item.getSortOrder() == null) {
                item.setSortOrder(0);
            }
            if (item.getIsRecommend() == null) {
                item.setIsRecommend(0);
            }
            if (item.getIsHot() == null) {
                item.setIsHot(0);
            }
            if (item.getSpicyLevel() == null) {
                item.setSpicyLevel(0);
            }
            
            menuItemService.save(item);
            return Result.success(item);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新菜品
     */
    @PutMapping("/items/{id}")
    @LogOperation(operation = "编辑菜品")
    public Result<MenuItem> updateMenuItem(@PathVariable Long id, @RequestBody MenuItem item) {
        try {
            MenuItem existing = menuItemService.getById(id);
            if (existing == null) {
                return Result.error("菜品不存在");
            }
            
            // 检查分类是否存在
            if (item.getCategoryId() != null) {
                MenuCategory category = menuCategoryService.getById(item.getCategoryId());
                if (category == null) {
                    return Result.error("分类不存在");
                }
            }
            
            // 如果没有提供编码，保持原有编码（编码由系统自动生成，前端不显示）
            if (item.getItemCode() == null || item.getItemCode().trim().isEmpty()) {
                item.setItemCode(existing.getItemCode());
            }
            
            item.setItemId(id);
            menuItemService.updateById(item);
            return Result.success(item);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除菜品
     */
    @DeleteMapping("/items/{id}")
    @LogOperation(operation = "删除菜品")
    public Result<Void> deleteMenuItem(@PathVariable Long id) {
        try {
            MenuItem item = menuItemService.getById(id);
            if (item == null) {
                return Result.error("菜品不存在");
            }
            menuItemService.removeById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

