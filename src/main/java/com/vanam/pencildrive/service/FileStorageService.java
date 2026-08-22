package com.vanam.pencildrive.service;
import com.vanam.pencildrive.dto.StoredFile;
import com.vanam.pencildrive.repo.FileMetadataRepo;
import jakarta.annotation.PostConstruct;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.*;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;


/**
    * This File Handles actual file Bytes.

    * "CRUD"    ->     Operations to upload file to s3 bucket
    *
    *
    * File storage service.
    * Handles actual file byte storage and retrieval.
    * This service is responsible for storing uploaded files, loading files for download,
    * deleting physical files, generating storage keys, and calculating file checksums.
    *
    * Current implementation may use local disk storage.
    * Later this service can be replaced with AWS S3 or another object storage system
    * without changing controller or business logic.
    */


@Service
public class FileStorageService {

    private static final Logger log =
            LoggerFactory.getLogger(FileStorageService.class);

    private final Path rootPath;

    public FileStorageService(
            @Value("${pencildrive.storage.local-path}") String localPath) {
        this.rootPath = Path.of(localPath)
                .toAbsolutePath()
                .normalize();
    }



    //<--------------------- 1. Create --------------------->

    /**
     * Need to develop workers architecture, Using Kafka or self developed.
     * steps:
     * 1. prepare batches
     * 2. save in cache/queue/tube
     * 3. push to cloud
     * 4. change file status from uploading -> ready in filesmetadata
     * */
    public StoredFile store(
            MultipartFile multipartFile,
            String storageKey,
            String contentType,
            String originalName
    ) {
        Path target = resolveSafe(storageKey);
        Path temporary = resolveSafe(storageKey + ".part");

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (
                    InputStream inputStream =
                            new DigestInputStream(
                                    multipartFile.getInputStream(),
                                    digest
                            )
            ) {

                Files.copy(inputStream, temporary);
            }
            try {
                Files.move(temporary, target, ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target);
            }
            String checksum =
                    HexFormat.of().formatHex(digest.digest());


            return new StoredFile(storageKey, checksum, Files.size(target));


        } catch (Exception e) {

            deletePathQuietly(temporary);
            deletePathQuietly(target);

            log.error(
                    "Failed to save file to local disk at path: {}",
                    storageKey,
                    e);

            throw new RuntimeException(
                    "Failed to store file",
                    e
            );
        }
    }





    //<--------------------- 2. Read --------------------->
    public Resource load(String storageKey) {
        try {

            Path filePath = resolveSafe(storageKey);


            if(!Files.isRegularFile(filePath)) {
                throw new RuntimeException(
                        "File Not Found In Storage"
                );
            }

            return new UrlResource(filePath.toUri());

        } catch (Exception e) {

            log.error(
                    "File Not Found In Storage"
            );
            throw new RuntimeException(
                    "Failed to load file",
                    e
            );
        }
    }




    public Path resolveSafe(String storageKey) {
        Path resolved = rootPath.resolve(storageKey).normalize();

        if(!resolved.startsWith(rootPath)) {
            throw new IllegalStateException(
                    "Invalid storage Key"
            );
        }

        return resolved;
    }

    public String generateStorageKey(String fileName) {


        return UUID.randomUUID() + extractSafeExtenstion(fileName);
        //return UUID.randomUUID() + "_" +rootPath.resolve(fileName).normalize();
    }

    public String normalizeOriginalName(String originalFilename) {

        if(originalFilename == null || originalFilename.isBlank()) {
            return "";
        }

        String normalized = originalFilename.replace('\\', '/');
        return normalized.substring(
                normalized.lastIndexOf('/') + 1
        );

    }
    public String extractSafeExtenstion(String baseName) {

        int dot = baseName.lastIndexOf('.');

        if(dot < 0) {
            return "";
        }

        String extension = baseName.substring(dot)
                .toLowerCase(Locale.ROOT);


        return extension.length() <= 16 ? extension: "";

    }

    private void deletePathQuietly(Path path) {

        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            log.warn("Unable to delete temporary file {}", path, e);
        }
    }

    public void deleteIfExists(String storageKey) {

        deletePathQuietly(resolveSafe(storageKey));
        deletePathQuietly(resolveSafe(storageKey + ".part"));
    }

    @PostConstruct
    public void initializeStorage() {
        try {
            Files.createDirectories(rootPath);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to initialize storage directory:" + rootPath,
                    e
            );
        }
    }
}
