package org.li.hotpot_reservationsystem.service.impl;

import org.li.hotpot_reservationsystem.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/api/uploads}")
    private String urlPrefix;

    // 允许的图片类型
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    // 最大文件大小（5MB）
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    public String uploadImage(MultipartFile file, String type) {
        // 验证文件
        validateFile(file);

        try {
            // 生成文件路径
            String fileName = generateFileName(file.getOriginalFilename());
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String relativePath = type + "/" + dateDir + "/" + fileName;
            
            // 解析上传路径（处理相对路径）
            Path basePath;
            if (uploadPath.startsWith("./") || uploadPath.startsWith(".\\")) {
                // 相对路径，转换为绝对路径（相对于项目根目录）
                String projectRoot = System.getProperty("user.dir");
                basePath = Paths.get(projectRoot, uploadPath.substring(2));
            } else {
                basePath = Paths.get(uploadPath);
            }
            
            // 创建目录
            Path uploadDir = basePath.resolve(type).resolve(dateDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 保存文件
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // 返回访问URL（确保URL格式正确）
            String url = urlPrefix;
            if (!url.endsWith("/")) {
                url += "/";
            }
            return url + relativePath;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件路径
            String urlPrefixNormalized = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
            String relativePath = fileUrl.replace(urlPrefixNormalized, "");
            
            // 解析上传路径（处理相对路径）
            Path basePath;
            if (uploadPath.startsWith("./") || uploadPath.startsWith(".\\")) {
                // 相对路径，转换为绝对路径（相对于项目根目录）
                String projectRoot = System.getProperty("user.dir");
                basePath = Paths.get(projectRoot, uploadPath.substring(2));
            } else {
                basePath = Paths.get(uploadPath);
            }
            
            Path filePath = basePath.resolve(relativePath);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 5MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型，仅支持 JPG、PNG、GIF、WEBP 格式");
        }
    }

    /**
     * 检查是否为允许的图片类型
     */
    private boolean isAllowedImageType(String contentType) {
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }
}

