package com.vanam.pencildrive.service;


import com.vanam.pencildrive.domain.FileGroupPermissions;
import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.enums.FileStatus;
import com.vanam.pencildrive.repo.FileGroupPermissionsRepo;
import com.vanam.pencildrive.repo.FileMetadataRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;


/**
     * Permission service.
     *
     * Handles all file permission and access-control logic.
     * This service checks whether a user can read, write, delete, share,
     * or manage a file.
     *
     * It supports:
     * 1. Owner-based access.
     * 2. Direct user-to-user file sharing.
     * 3. Group-based file sharing.
     *
     * Controllers and file services should call this service before returning,
     * modifying, deleting, or sharing protected file data.
     */

@Service
public class PermissionService {


    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);
    private final FileMetadataRepo fileMetadataRepo;
    private final FileGroupPermissionsRepo fileGroupPermissionsRepo;
    public PermissionService(
            FileMetadataRepo fileMetadataRepo,
            FileGroupPermissionsRepo fileGroupPermissionsRepo
    ) {
        this.fileMetadataRepo = fileMetadataRepo;
        this.fileGroupPermissionsRepo = fileGroupPermissionsRepo;
    }



    //<------------------------------------| Adding Existing File into the Group |------------------------------------>
    public boolean addFile(Groups groupId, FilesMetadata file, User ownerId) {

        /** 1. Check Whether File is Empty. */
        if(file == null) {
            return false;
        }


        /** 2. Check Whether File Matches User And Data*/

        Optional<FilesMetadata> verifiedFile = null;
        try {
            verifiedFile =
                    fileMetadataRepo.findByOwnerIdAndIdAndStatus(file, ownerId, FileStatus.READY);

            if(verifiedFile.isEmpty()) { return false; }

        } catch(RuntimeException e) {
            if(e instanceof org.springframework.dao.DataAccessException) {
                log.error("Confirmed: This is a database error ! -> Class : PermissionService.addFile.Line -> no.65");
            } else {
                log.warn(
                        ownerId + " " +file.getId() + " " +groupId.getId() + " Doesn't match, Cause Malicious attempt" +
                        "Malicous Activity detected" + e.toString() + "" + ownerId + "" +
                        "User Doesn't Have Any permission to perform this task {PermissionService/addFile}");
            }
            return false;
        }

        /** 3. Create Instance Of FileGroupPermissions Class*/
        FileGroupPermissions pendingFileGroupPermission =
                FileGroupPermissions.createdUploading(
                        verifiedFile.get(),
                        groupId,
                        15,
                        ownerId
                );

        /** 4. Save And Flush Object*/
        try {
            fileGroupPermissionsRepo.saveAndFlush(pendingFileGroupPermission);
        } catch (RuntimeException e) {
            if(e instanceof org.springframework.dao.DataAccessException) {
                log.error("Confirmed: This is a database error ! -> Class : PermissionService.addFile.Line -> no.65");
            } else {
                log.warn(
                        ownerId + " " +file.getId() + " " +groupId.getId() + " Doesn't match, Cause Malicious attempt"
                        + "Malicous Activity detected");
            }
            return false;
        }
        return true;

    }


}
