package com.csye6220.huskyamazon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 URL 路径 "/uploads/**" 到本地文件系统目录
        // 比如: http://localhost:8080/uploads/abc.jpg -> /Users/dbaa/Documents/HuskyUploads/abc.jpg

        // 注意：Windows 上路径需要以 "file:///" 开头，Mac/Linux 是 "file:"
        // 为了跨平台，我们可以这样拼：
        String resourceLocation = "file:" + uploadDir;
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);

        // 同时保留默认的静态资源映射 (css, js)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}