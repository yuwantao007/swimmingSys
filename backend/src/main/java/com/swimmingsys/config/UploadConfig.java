package com.swimmingsys.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 文件上传配置类
 */
@Configuration
public class UploadConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，用于访问上传的文件
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(3600); // 设置缓存时间
    }
    
    /**
     * 应用启动时创建必要的上传目录
     */
    @PostConstruct
    public void initUploadDirectories() {
        // 使用与资源处理器相同的路径
        String baseUploadPath = System.getProperty("user.dir") + "/uploads/";
        
        // 创建主上传目录
        File uploadDir = new File(baseUploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 创建文章相关子目录
        File articleDir = new File(baseUploadPath + "article");
        if (!articleDir.exists()) {
            articleDir.mkdirs();
        }
        
        // 创建按年月日的目录结构
        java.time.LocalDate today = java.time.LocalDate.now();
        String yearPath = baseUploadPath + "article/" + today.getYear();
        String monthPath = yearPath + "/" + today.getMonthValue();
        String dayPath = monthPath + "/" + today.getDayOfMonth();
        
        File yearDir = new File(yearPath);
        if (!yearDir.exists()) {
            yearDir.mkdirs();
        }
        
        File monthDir = new File(monthPath);
        if (!monthDir.exists()) {
            monthDir.mkdirs();
        }
        
        File dayDir = new File(dayPath);
        if (!dayDir.exists()) {
            dayDir.mkdirs();
        }
    }
}