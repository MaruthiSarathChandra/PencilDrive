package com.vanam.pencildrive.dto;

public class SignUpRequest {

    private String userId;
    private String email;
    private String password;



    //Getter & Setter

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setEmail(String email) {this.email = email;}

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() { return userId; }
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
