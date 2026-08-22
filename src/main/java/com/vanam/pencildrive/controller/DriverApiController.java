package com.vanam.pencildrive.controller;


import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.dto.*;
import com.vanam.pencildrive.service.DriveFileService;
import com.vanam.pencildrive.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/drive/data")
public class DriverApiController {



    /**
     * 0. Profile {
     * 0.1 update Profile
     * 0.2 Delete Profile
     * 0.3 Orginization/User
     * 0.4 PencilDrive Application Owner Administor
     * }
     * 1. Projects
     * 2. MyDrive                                           //check
     * 3. Shared with Me
     * 4. Starred
     * 5. Trash Bin {Not Delete File}
     * 6. Storage Used / Space Availalbe                    //check
     * 7. Upload                                            //check
     * 8. Get File (View) / Create File (txt, powerpoint, excel, word) / Download
     * 9. Delete
     * 10. Permission / Update Permission
     * 11. AI Agent
     * 12. Get More Storage {Related to Payments and Purchases}
     *
     **/
    private final DriveFileService driveFileService;
    private final GroupService groupService;


    public DriverApiController(DriveFileService driveFileService, GroupService groupService) {

        this.driveFileService = driveFileService;
        this.groupService = groupService;

    }


    @GetMapping("/projects")
    public ResponseEntity<List<String>> getProjects() {

        //Fake Code, Develop Code
        List<String> array = new ArrayList<>();
        return ResponseEntity.ok(array);
    }

    @PostMapping("/create-group")
    public ResponseEntity<GroupResponse> createGroup(@RequestBody CreateGroupRequest createGroupRequest) {
        return ResponseEntity.ok(groupService.createGroup(createGroupRequest));
    }

    @GetMapping("/files/my-drive")
    public ResponseEntity<Slice<FileResponse>> getMyDrive(@RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok(driveFileService.getMyDriveFiles(page));
    }

    @GetMapping("/files/sharedwithme")
    public ResponseEntity<List<String>> getSharedWithMe() {


        //Fake Code, Develop Code
        List<String> array = new ArrayList<>();
        return ResponseEntity.ok(array);

    }

    @GetMapping("/files/starred")
    public ResponseEntity<List<String>> getStarred() {


        //Fake Code, Develop Code
        List<String> array = new ArrayList<>();
        return ResponseEntity.ok(array);
    }

    @PutMapping("/files/{id}/star")
    public ResponseEntity<MessageResponse> starFile(@PathVariable Long id) { //update need Id return type regarding to System Architecture / Database(entity) Architecture


        //Fake Code, Develop Code

        return ResponseEntity.ok( new MessageResponse("File starred successfully"));
    }

    @GetMapping("/trash")
    public ResponseEntity<List<String>> getTrash() {

        //Fake Code, Develop Code
        List<String> array = new ArrayList<>();
        return ResponseEntity.ok(array);
    }

    @GetMapping("/files/storage")
    public ResponseEntity<MessageResponse> getStorage() {

        //Fake Code, Develop Code
        return ResponseEntity.ok( new MessageResponse("File starred successfully"));
    }

    @PostMapping("/files/upload")
    public ResponseEntity<UploadFileResponse> upload(@RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(driveFileService.uploadFile(file));
    }


    @GetMapping("/files/{file}")
    public ResponseEntity<List<String>> getFile(@PathVariable String fileName) {

        //Fake Code, Develop Code
        List<String> array = new ArrayList<>();
        return ResponseEntity.ok(array);
    }












}
