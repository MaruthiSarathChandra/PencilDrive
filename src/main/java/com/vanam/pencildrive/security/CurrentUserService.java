package com.vanam.pencildrive.security;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.repo.LoginRepo;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class CurrentUserService {

    private static final Logger log = LoggerFactory.getLogger(CurrentUserService.class);
    private final LoginRepo loginRepo;

    public CurrentUserService(LoginRepo loginRepo) { this.loginRepo = loginRepo; }

    public String getCurrentUserEmail() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()) {

            /** FUTURE CODE REQUIREMENT / EDGE CASE:
             * 1. if authentication is null, this points to an issue up stream (e.g., JWTAuthenticationFilter).
             * 2. Handles scenarios where a user deletes their account but possesses an unexpired JWT token.
             *      -> Possible error from JWTAuthenticationFilter, securityFilter.
             *          ->And User actually deleted account but still access to JWT Token.
             */

            log.warn("SECURITY_NO_AUTHENTICATION_IN_CONTEXT");
            throw new AuthenticationCredentialsNotFoundException("Authentication Required");
        }

        // This extracts the 'email' you put into UsernamePasswordAuthenticationToken instantly from memory
        return authentication.getName();
    }

    public Optional<User> requireCurrentUserObject() {
        return loginRepo
                .findByEmailId
                        (getCurrentUserEmail());
    }

}
