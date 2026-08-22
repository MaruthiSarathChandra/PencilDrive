package com.vanam.pencildrive.dto;

import org.springframework.stereotype.Component;

@Component
public class LoginRequest {

    private String email;
    private String password;



    //Getter & Setter
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
