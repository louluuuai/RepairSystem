package com.residence.repair.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriétés de stockage local des médias.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /**
     * Répertoire local où les médias sont stockés.
     */
    private String uploadDir = "./uploads";

    /**
     * Préfixe public utilisé pour générer les URLs.
     */
    private String publicBaseUrl = "/media";
}
