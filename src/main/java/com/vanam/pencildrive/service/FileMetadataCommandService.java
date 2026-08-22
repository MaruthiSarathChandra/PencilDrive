package com.vanam.pencildrive.service;
import com.vanam.pencildrive.CustomException.StorageLimitExceededException;
import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.enums.FileStatus;
import com.vanam.pencildrive.repo.FileMetadataRepo;
import com.vanam.pencildrive.repo.LoginRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;


@Service
public class FileMetadataCommandService {
    private final FileMetadataRepo fileMetadataRepo;
    private final LoginRepo loginRepo;


    public FileMetadataCommandService(
            FileMetadataRepo fileMetadataRepo,
            LoginRepo loginRepo
    ) {
        this.fileMetadataRepo = fileMetadataRepo;
        this.loginRepo = loginRepo;
    }


    @Transactional
    public FilesMetadata beginUpload(
            Long ownerId,
            String originalName,
            String contentType,
            long sizeBytes,
            String storageKey
    ) {
        int reserved = loginRepo.reserveStorage(
                ownerId,
                sizeBytes
        );

        if(reserved != 1) {
            throw new StorageLimitExceededException(
                    "Storage limit exceeded"
            );
        }

        User owner = loginRepo.getReferenceById(ownerId);

        FilesMetadata metadata = FilesMetadata.createUploading(
                owner,
                originalName,
                contentType,
                sizeBytes,
                storageKey
        );

        return fileMetadataRepo.saveAndFlush(metadata);
    }


    @Transactional
    public FilesMetadata completeUpload(
            Long fileId,
            String checkSum
    ) throws FileNotFoundException {
        FilesMetadata metadata = fileMetadataRepo.findById(fileId)
                .orElseThrow(() ->
                        new FileNotFoundException(
                                "File metadata not found"
                        )
                );
        metadata.markReady(checkSum);
        return fileMetadataRepo.saveAndFlush(metadata);
    }



    @Transactional
    public void failUpload(
            Long fileId,
            Long ownerId,
            long fileSize
    )throws FileNotFoundException {
        FilesMetadata metadata = fileMetadataRepo.findById(fileId)
                .orElseThrow(() ->
                        new FileNotFoundException(
                                "File metadata not found")
                );

        if(metadata.getStatus() == FileStatus.READY) {
            return;
        }
        metadata.markFailed();
        fileMetadataRepo.saveAndFlush(metadata);

        int released = loginRepo.releaseStorage(
                ownerId,
                fileSize
        );

        if(released != 1) {

            throw new IllegalStateException(
                    "Failed to release reserved storage"
            );
        }
    }

    @Transactional
    public void deleteFile
            (Long fileId,
             Long userId
    ) {
    }

}
