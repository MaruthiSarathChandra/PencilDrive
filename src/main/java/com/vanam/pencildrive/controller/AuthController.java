package com.vanam.pencildrive.controller;
import com.vanam.pencildrive.dto.AuthResponse;
import com.vanam.pencildrive.dto.LoginRequest;
import com.vanam.pencildrive.dto.SignUpRequest;
import com.vanam.pencildrive.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/api")
public class AuthController {


    @Autowired
    private UserService authService;


    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> loginController(@RequestBody LoginRequest request, HttpServletResponse response) {
        System.out.println(request.getEmail());
        AuthResponse authResponse = this.authService.login(request);

        if(authResponse.isSuccess()) {
            Cookie cookie = new Cookie("jwtToken", authResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);

            response.addCookie(cookie);
        }
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> registerController(@RequestBody SignUpRequest request) {
        AuthResponse response = this.authService.register(request);
        return ResponseEntity.ok(response);
    }
}

