package org.li.hotpot_reservationsystem.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    
    /**
     * 上传图片
     * @param file 文件
     * @param type 文件类型：menu-菜品图片, avatar-头像, review-评论图片
     * @return 文件访问URL
     */
    String uploadImage(MultipartFile file, String type);
    
    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);
}

