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
        // Map the URL path "/uploads/**" to a local file system directory.
        // 比如: http://localhost:8080/uploads/abc.jpg -> /Users/dbaa/Documents/HuskyUploads/abc.jpg

        // Note: On Windows, paths need to start with "file:///", while on Mac/Linux it's "file:".
        // For cross-platform compatibility, we can construct the path like this:
        String resourceLocation = "file:" + uploadDir;
        if (!resourceLocation.endsWith("/")) {
            resourceLocation += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);

        // While also retaining the default static resource mapping. (css, js)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}