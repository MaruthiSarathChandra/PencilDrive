package com.vanam.pencildrive.service;

import com.vanam.pencildrive.dto.AuthResponse;
import com.vanam.pencildrive.dto.LoginRequest;
import com.vanam.pencildrive.dto.SignUpRequest;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.enums.AccountPlan;
import com.vanam.pencildrive.enums.AccountRole;
import com.vanam.pencildrive.enums.AccountStatus;
import com.vanam.pencildrive.repo.LoginRepo;
import com.vanam.pencildrive.security.JwtService;
import com.vanam.pencildrive.security.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private LoginRepo loginRepo;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;

    public UserService(LoginRepo loginRepo, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.loginRepo = loginRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }


    public AuthResponse login(LoginRequest request) {
        Optional<User> user = loginRepo.findByEmailId(request.getEmail());

        if(user.isEmpty()) {
            return new AuthResponse(false, "Invalid email or password", null);
        } else {
            Boolean matched = passwordEncoder.matches(request.getPassword(), user.get().getPassword());

            if(!matched) {
                return new AuthResponse(false, "Invalid email or password", null);
            }

            String token = jwtService.generateToken(request.getEmail());
            return new AuthResponse(true, "Login Successful", token);
        }
    }


    public AuthResponse register(SignUpRequest request) {
        boolean exist = loginRepo.findByEmailId(request.getEmail()).isPresent();


        if (loginRepo.findByEmailId(request.getEmail()).isPresent()) {
            return new AuthResponse(false, "Email Exists!. Use Different email", null);

        } else {
            User obj = new User();
            obj.setUsername(request.getUserId());
            obj.setEmailId(request.getEmail());
            String hashpassword = passwordEncoder.hashPassword(request.getPassword());
            obj.setPassword(hashpassword);
            obj.setStorageLimit(5*1024*1024*1024L);
            obj.setStorageUsed(0L);
            obj.setRole(AccountRole.ADMIN);
            obj.setPlan(AccountPlan.PLATINUM);
            obj.setStatus(AccountStatus.ACTIVE);

            loginRepo.save(obj);
            return new AuthResponse(true, "Registration Successful", null);

        }
    }
}
