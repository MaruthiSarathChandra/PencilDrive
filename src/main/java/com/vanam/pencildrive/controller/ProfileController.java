package com.vanam.pencildrive.controller;
import com.vanam.pencildrive.dto.ProfileResponse;
import com.vanam.pencildrive.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    /**
     * 0. Profile {
     *      0.1 update Profile
     *      0.2 Delete Profile
     *      0.3 Orginization/User
     *      0.4 PencilDrive Application Owner Administor
     * }
     * */

    @Autowired
    private ProfileService profileService;

    @GetMapping("/1")
    public ResponseEntity<ProfileResponse> getProfile() {

        ProfileResponse response = profileService.getProfile();
        if ("200".equals(response.getMessage())) { return ResponseEntity.ok(response);}

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }


    @PutMapping("/password")
    public void updatePassword() {

    }

}
