package com.vanam.pencildrive.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.enums.FileStatus;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FileResponse(

        String message,
        String owner,

        Long id,
        String fileName,
        String originalName,
        String contentType,
        Long sizeBytes,
        FileStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {


    public static FileResponse from(String owner, FilesMetadata file) {
        return new FileResponse(
                null,
                owner,
                file.getId(),
                file.getFileName(),
                file.getStorageKey(),
                file.getContentType(),
                file.getSizeBytes(),
                file.getStatus(),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }

    public static FileResponse from(String message, String email, FilesMetadata file) {
        return new FileResponse(
                message,
                email,
                file.getId(),
                file.getFileName(),
                file.getStorageKey(),
                file.getContentType(),
                file.getSizeBytes(),
                file.getStatus(),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }
}