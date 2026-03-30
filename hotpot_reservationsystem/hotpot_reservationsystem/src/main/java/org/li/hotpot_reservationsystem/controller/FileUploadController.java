package org.li.hotpot_reservationsystem.controller;

import org.li.hotpot_reservationsystem.common.Result;
import org.li.hotpot_reservationsystem.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传Controller
 */
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 上传图片
     * @param file 文件
     * @param type 文件类型：menu-菜品图片, avatar-头像, review-评论图片
     * @return 文件URL
     */
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "common") String type) {
        // 验证类型
        if (!isValidType(type)) {
            throw new IllegalArgumentException("不支持的文件类型，type 必须是: menu, avatar, review 或 common");
        }

        String url = fileUploadService.uploadImage(file, type);
        
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        
        return Result.success(result);
    }

    /**
     * 删除文件
     * @param url 文件URL
     * @return 删除结果
     */
    @DeleteMapping("/image")
    public Result<Void> deleteImage(@RequestParam("url") String url) {
        boolean deleted = fileUploadService.deleteFile(url);
        if (!deleted) {
            throw new RuntimeException("文件不存在或删除失败");
        }
        return Result.success();
    }

    /**
     * 验证文件类型
     */
    private boolean isValidType(String type) {
        return "menu".equals(type) || "avatar".equals(type) || 
               "review".equals(type) || "common".equals(type);
    }
}

