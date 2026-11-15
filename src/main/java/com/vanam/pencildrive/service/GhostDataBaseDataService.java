package com.vanam.pencildrive.service;


import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.dto.AuthResponse;
import com.vanam.pencildrive.dto.FileResponse;
import com.vanam.pencildrive.dto.UploadFileResponse;
import com.vanam.pencildrive.enums.AccountPlan;
import com.vanam.pencildrive.enums.AccountRole;
import com.vanam.pencildrive.enums.AccountStatus;
import com.vanam.pencildrive.repo.FileMetadataRepo;
import com.vanam.pencildrive.repo.GroupsRepo;
import com.vanam.pencildrive.repo.LoginRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class GhostDataBaseDataService {

    private final GroupsRepo groupsRepo;
    private final FileMetadataRepo fileMetadataRepo;
    private final LoginRepo loginRepo;
    private final FileMetadataCommandService commandService;
    private final FileStorageService fileStorageService;

    @PersistenceContext
    private EntityManager entityManager;


    public GhostDataBaseDataService(
            GroupsRepo groupsRepo,
            FileMetadataRepo fileMetadataRepo,
            LoginRepo loginRepo,
            FileMetadataCommandService commandService,
            FileStorageService fileStorageService
    ){
        this.groupsRepo = groupsRepo;
        this.fileMetadataRepo = fileMetadataRepo;
        this.loginRepo = loginRepo;
        this.commandService = commandService;
        this.fileStorageService = fileStorageService;
    }


    public AuthResponse ghostAccountCreation(){

        User obj = new User();
        obj.setUsername("Deleted User");
        obj.setEmail("deleted_user@pencildrive.local");
        String hashpassword = ("DISABLED_ACCOUNT");
        obj.setPassword(hashpassword);
        obj.setStorageLimit(1L);
        obj.setStorageUsed(0L);
        obj.setRole(AccountRole.DELETED);
        obj.setPlan(AccountPlan.DISABLED);
        obj.setStatus(AccountStatus.DELECTED);

        loginRepo.save(obj);
        return new AuthResponse(true, "Registration Successful", null);
    }




    public UploadFileResponse createGhostFileMetadata() {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FilesMetadata pending =             //status = uploading
                    commandService.beginUpload(
                            1L,
                            "GHOSTFILE",
                            "Ghost/deleted",
                            1,
                            fileStorageService.generateStorageKey("GHOSTFILE")
                    );
            FilesMetadata completed =               // error
                    commandService.completeUpload(
                            pending.getId(),
                            HexFormat.of().formatHex(digest.digest())
                    );

            return new UploadFileResponse(
                    "File upload successfully",
                    FileResponse.from(
                            "DeletedUser",
                            completed)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void createGhostGroup() {

        User user =
                entityManager.getReference(User.class, 1);

        System.out.println(user.getEmail());

        Groups groups = Groups.createGroup(
                user,
                "GHOST-GROUP"
        );

        groupsRepo.save(groups);
    }
}
