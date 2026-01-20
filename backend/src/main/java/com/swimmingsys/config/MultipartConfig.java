package com.swimmingsys.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;

/**
 * 文件上传配置类
 * 配置文件上传大小限制
 */
@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // 设置单个文件最大大小为10MB
        factory.setMaxFileSize(org.springframework.util.unit.DataSize.ofMegabytes(10));
        
        // 设置总请求最大大小为50MB
        factory.setMaxRequestSize(org.springframework.util.unit.DataSize.ofMegabytes(50));
        
        return factory.createMultipartConfig();
    }
}