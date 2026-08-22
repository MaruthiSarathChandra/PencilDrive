package com.vanam.pencildrive.service;
import com.vanam.pencildrive.dto.ProfileResponse;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.enums.AccountStatus;
import com.vanam.pencildrive.repo.LoginRepo;
import com.vanam.pencildrive.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.util.Optional;


/**
     * Profile service.
     {
         * 0. Profile
         * 0.1 update profile
         * 0.2 delete profile
         * 0.3 organization/user type
         * 0.4 admin/user role
     }

     * Handles user profile-related business operations.
     * This includes getting profile data, updating profile information,
     * deleting/deactivating a profile, and returning storage/user account details.

     * This service should work with the User repository and should never expose
     * sensitive fields such as password hashes in API responses.
     */


@Service
public class ProfileService {

    private final CurrentUserService currentUserService;
    private final LoginRepo userRepository;
    public ProfileService(CurrentUserService currentUserService, LoginRepo userRepository) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }




    // 1. Profile
    public ProfileResponse getProfile() {

        String emailId = currentUserService.getCurrentUserEmail();
        System.out.println(emailId);


        //2. Query the database exactly ONCE. Throw exception if user is missing / inactive.
        // Retrieve User Related Data
        Optional<User> user = userRepository.findUserByEmailIdAndStatus(emailId, AccountStatus.ACTIVE);

        if(user.isEmpty()) {
            return new ProfileResponse(
                    "401 Session Invalid, Login Again", null,
                    null, null, null,
                    null, null, null);
        }


        // 3. Map values directly to Response object in matching constructor order: (username, email, message)
        return new ProfileResponse("200", user.get().getUsername(), user.get().getStatus(),
                user.get().getEmailId(), user.get().getRole(),
                user.get().getPlan(), user.get().getStorageLimit(), user.get().getStorageUsed());
    }


}
