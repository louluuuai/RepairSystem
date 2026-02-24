package com.residence.repair.service;

import com.residence.repair.config.StorageProperties;
import com.residence.repair.domain.enums.MediaType;
import com.residence.repair.exception.ApiException;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Service métier pour le stockage local des fichiers médias.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaStorageService {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "avi", "mkv", "webm");

    private final StorageProperties storageProperties;

    private Path uploadPath;

    /**
     * Initialisation du dossier de stockage au démarrage.
     */
    @PostConstruct
    public void init() {
        try {
            uploadPath = Paths.get(storageProperties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            log.info("Media storage directory initialized at '{}'.", uploadPath);
        } catch (IOException e) {
            throw new ApiException("STORAGE_INIT_FAILED", "Cannot initialize media storage", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Stocker un fichier média et retourner sa référence publique.
     */
    public StoredMedia store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("INVALID_FILE", "Uploaded file is empty", HttpStatus.BAD_REQUEST);
        }

        MediaType mediaType = resolveMediaType(file.getContentType());
        if (mediaType == null) {
            throw new ApiException("UNSUPPORTED_MEDIA_TYPE", "Only image/* or video/* files are allowed", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        String extension = resolveAndValidateExtension(file.getOriginalFilename(), mediaType);
        String generatedFileName = UUID.randomUUID() + "." + extension;
        Path target = uploadPath.resolve(generatedFileName).normalize();
        if (!target.startsWith(uploadPath)) {
            throw new ApiException("INVALID_FILE_PATH", "Invalid file path", HttpStatus.BAD_REQUEST);
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store media file '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            throw new ApiException("STORAGE_WRITE_FAILED", "Failed to store media file", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String publicUrl = buildPublicUrl(generatedFileName);
        return new StoredMedia(publicUrl, mediaType, generatedFileName, file.getSize());
    }

    /**
     * Déduire le type média à partir du Content-Type HTTP.
     */
    private MediaType resolveMediaType(String contentType) {
        if (contentType == null) {
            return null;
        }
        if (contentType.startsWith("image/")) {
            return MediaType.IMAGE;
        }
        if (contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        }
        return null;
    }

    /**
     * Valider l'extension selon le type du média.
     */
    private String resolveAndValidateExtension(String originalFilename, MediaType mediaType) {
        String extension = extractExtension(originalFilename);
        if (extension == null) {
            return defaultExtension(mediaType);
        }

        Set<String> allowed = mediaType == MediaType.IMAGE ? IMAGE_EXTENSIONS : VIDEO_EXTENSIONS;
        if (!allowed.contains(extension)) {
            throw new ApiException("INVALID_FILE_EXTENSION", "File extension does not match media type", HttpStatus.BAD_REQUEST);
        }
        return extension;
    }

    /**
     * Extraire une extension normalisée en minuscules.
     */
    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank() || !filename.contains(".")) {
            return null;
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return extension.isBlank() ? null : extension;
    }

    /**
     * Donner une extension par défaut si le nom d'origine n'en contient pas.
     */
    private String defaultExtension(MediaType mediaType) {
        return mediaType == MediaType.IMAGE ? "jpg" : "mp4";
    }

    /**
     * Construire une URL publique stable.
     */
    private String buildPublicUrl(String fileName) {
        String baseUrl = storageProperties.getPublicBaseUrl();
        if (baseUrl.endsWith("/")) {
            return baseUrl + fileName;
        }
        return baseUrl + "/" + fileName;
    }

    @Getter
    @AllArgsConstructor
    public static class StoredMedia {
        private final String url;
        private final MediaType type;
        private final String fileName;
        private final long size;
    }
}
