package com.residence.repair.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration de publication des fichiers statiques locaux.
 */
@Configuration
@RequiredArgsConstructor
public class StaticResourceConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
        String publicPattern = storageProperties.getPublicBaseUrl() + "/**";
        String resourceLocation = uploadPath.toUri().toString();
        registry.addResourceHandler(publicPattern).addResourceLocations(resourceLocation);
    }
}
