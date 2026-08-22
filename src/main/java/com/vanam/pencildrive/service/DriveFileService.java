package com.vanam.pencildrive.service;
import com.vanam.pencildrive.CustomException.FileNotFoundException;
import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.dto.FileResponse;
import com.vanam.pencildrive.dto.MessageResponse;
import com.vanam.pencildrive.dto.StoredFile;
import com.vanam.pencildrive.dto.UploadFileResponse;
import com.vanam.pencildrive.enums.FileStatus;
import com.vanam.pencildrive.repo.FileMetadataRepo;
import com.vanam.pencildrive.repo.LoginRepo;
import com.vanam.pencildrive.security.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;


/*
        *
        *  This File Service Responsible to handle all
        *
        *  "Create"    ->     operations
        *  "Read"      ->     operations
        *  "Update"    ->     operations
        *  "Delete"    ->     operations
        *
        *   of files metadata
        *
        * */
@Service
public class DriveFileService {

    private final FileStorageService fileStorageService;
    private final FileMetadataCommandService commandService;
    private final FileMetadataRepo fileMetadataRepo;
    private final CurrentUserService currentUserService;
    private final LoginRepo loginRepo;

    public DriveFileService(
            FileStorageService fileStorageService,
            FileMetadataRepo fileMetadataRepo,
            LoginRepo loginRepo,
            CurrentUserService currentUserService,
            FileMetadataCommandService commandService
    ){
        this.fileStorageService = fileStorageService;
        this.fileMetadataRepo = fileMetadataRepo;
        this.loginRepo = loginRepo;
        this.currentUserService = currentUserService;
        this.commandService = commandService;
    }

    private static final Logger log = LoggerFactory.getLogger(DriveFileService.class);
    private static final int PAGE_SIZE = 50;
    public UploadFileResponse uploadFile(MultipartFile file) {

        validateUpload(file);

        Optional<User> owner = currentUserService.requireCurrentUserObject();


        if(owner.isEmpty()) {
            log.warn("File upload failed: No user found in database for email [{}]",
                    SecurityContextHolder.getContext().getAuthentication());
            return new UploadFileResponse("Something went wrong, Try again", null);
        }

        if(owner.get().getStorageLimit() < (owner.get().getStorageUsed() + file.getSize())) {
            return new UploadFileResponse( "storage is full, upgrade to pro level", null);
        }

        String originalName =
                fileStorageService.normalizeOriginalName(file.getOriginalFilename());

        //String contentType = fileStorageService.extractSafeExtenstion(originalName);

        String contentType =
                file.getContentType() != null
                        ? file.getContentType()
                        : "application/octet-stream";

        String storageKey = fileStorageService.generateStorageKey(originalName);


        FilesMetadata pending =             //status = uploading
                commandService.beginUpload(
                        owner.get().getId(),
                        originalName,
                        contentType,
                        file.getSize(),
                        storageKey
                );


        try{
            StoredFile storedFile = fileStorageService
                    .store( file,
                            storageKey,
                            contentType,
                            originalName
                    );

            FilesMetadata completed =               // error
                    commandService.completeUpload(
                            pending.getId(),
                            storedFile.checkSum()
                    );

            return new UploadFileResponse(
                    "File upload successfully",
                    FileResponse.from(
                            currentUserService.getCurrentUserEmail(),
                            completed)
            );

        } catch (Exception e) {
            fileStorageService.deleteIfExists(storageKey);

            try {
              commandService.failUpload(
                      pending.getId(),
                      owner.get().getId(),
                      file.getSize());

            } catch (Exception ex) {
                log.error(
                        "Failed upload compensation for fileId={}",
                        pending.getId(),
                        commandService
                );
            }
            log.warn("Filed to save entity instance" + e.toString());
            return new UploadFileResponse( "Something went wrong, Try again", null);
        }
    }


    @Transactional(readOnly = true)
    public Slice<FileResponse> getMyDriveFiles(int page) {

        /** Read Operations
         *
         * fetch folder files and single files
         *
         * To Fetch Folder -> we need to only look for files which has parent_folder = null
         * To Fetch Files -> we need to only look for files which has parent_folder = null
         * TO Fetch Groups -> we need to fetch
         *
         * */


        Pageable pageable = PageRequest.of(
                page,
                50,
                Sort.by(Sort.Direction.DESC, "createdAt")
                );

        return fileMetadataRepo
                .findByOwner_IdAndStatus(
                        loginRepo
                                .findIdByEmailId(
                                        currentUserService
                                                .getCurrentUserEmail()
                                ).get(), // this is to get current user gmail and fetch owner
                        FileStatus.READY,
                        pageable)
                .map(this::mapToResponse);
    }
    
    
    public MessageResponse deleteFile(Long fileId) {
        
        Optional<User> user =
                currentUserService.requireCurrentUserObject();


        if(user.isEmpty()) {
            log.warn("File upload failed: No user found in database for email [{}]",
                    SecurityContextHolder.getContext().getAuthentication());
            return new MessageResponse("Something went wrong, Try again");
        }

        Optional<FilesMetadata> metadata =
                fileMetadataRepo.findById(fileId);


        if(metadata.isEmpty()) {
            log.info("File Unable to fetch: [{}], could be a cause of testing / hack / no data exist");
            throw new FileNotFoundException(user.get().getId() + "" + fileId);
        }

        try {



        } catch (Exception e) {
            log.error(e.toString());
            return new MessageResponse("Something went wrong");
        }


        
        // Check for best fit of current dto or need new dto to send effective scaling 
        return new MessageResponse("Success");
    }


    private FileResponse mapToResponse(FilesMetadata file) {
        return FileResponse.from("Success", currentUserService.getCurrentUserEmail(), file);
    }




    private void validateUpload(MultipartFile file) {

        if(file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Upload file cannot be empty"
            );
        }
        if(file.getSize() <= 0) {
            throw new IllegalArgumentException(
                    "Invalid file size"
            );
        }
    }


    

    private void deletePathQuietly(Path path) {

        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            log.warn("Unable to delete temporary file {}", path, e);
        }
    }

}
