package org.li.hotpot_reservationsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC配置类
 * 配置CORS跨域请求、静态资源映射等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 文件上传路径（从配置文件读取，默认为./uploads）
     */
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 配置CORS跨域请求
     * 允许所有来源的跨域请求，支持常用的HTTP方法
     * 
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源映射
     * 将/uploads/**路径映射到文件上传目录，使上传的文件可以通过HTTP访问
     * 
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /api/uploads/** 映射到文件上传目录
        String uploadDir;
        if (uploadPath.startsWith("./") || uploadPath.startsWith(".\\")) {
            // 相对路径，转换为绝对路径（相对于项目根目录）
            String projectRoot = System.getProperty("user.dir");
            uploadDir = Paths.get(projectRoot, uploadPath.substring(2)).toAbsolutePath().toString();
        } else {
            uploadDir = Paths.get(uploadPath).toAbsolutePath().toString();
        }
        uploadDir = uploadDir.replace("\\", "/");
        
        // 确保路径以 / 结尾
        if (!uploadDir.endsWith("/")) {
            uploadDir += "/";
        }
        
        // 配置静态资源映射
        // context-path 是 /api，所以 /uploads/** 会被映射到 /api/uploads/**
        // 但为了确保正确，我们使用完整路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir)
                .setCachePeriod(3600); // 缓存1小时
    }
}

