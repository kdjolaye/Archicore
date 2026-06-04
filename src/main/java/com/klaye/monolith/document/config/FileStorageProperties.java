package com.klaye.monolith.document.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Propriétés de configuration pour le stockage des fichiers.
 * Centralisé dans le Monolithe.
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

    private String rootLocation;
    private int maxSizeMb = 20;
    private List<String> allowedTypes = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/jpeg",
            "image/png");

}
