package com.vanam.pencildrive.controller;

import com.vanam.pencildrive.dto.AuthResponse;
import com.vanam.pencildrive.dto.UploadFileResponse;
import com.vanam.pencildrive.service.GhostDataBaseDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/api/ghost")
public class GhostDataBaseDataController {

    @Autowired
    private GhostDataBaseDataService ghostDataBaseDataService;

    @PostMapping("/create-ghost-user")
    public ResponseEntity<AuthResponse> createGhostAccount() {
        return ResponseEntity.ok(ghostDataBaseDataService.ghostAccountCreation());
    }

    @PostMapping("/create-ghost-file")
    public ResponseEntity<UploadFileResponse> createGhostFile() {
        return ResponseEntity.ok(ghostDataBaseDataService.createGhostFileMetadata());
    }

    @PostMapping("/create-ghost-group")
    public ResponseEntity<Void> createGhostGroup() {
        ghostDataBaseDataService.createGhostGroup();
        return ResponseEntity.noContent().build();
    }
}
