package com.vanam.pencildrive.dto;
import com.vanam.pencildrive.enums.AccountPlan;
import com.vanam.pencildrive.enums.AccountRole;
import com.vanam.pencildrive.enums.AccountStatus;
import jakarta.annotation.Nullable;

public class ProfileResponse {


    private String userName;
    private String email;
    private String message;
    private AccountRole role;
    private AccountPlan plan;
    private Long storageLimit;
    private Long storageUsed;
    private AccountStatus status;


    public ProfileResponse(@Nullable String message, @Nullable String userName, @Nullable AccountStatus status,
                           @Nullable String email, @Nullable AccountRole role,
                           @Nullable AccountPlan plan, @Nullable Long storageLimit, @Nullable Long storageUsed) {
        this.message = message;
        this.userName = userName;
        this.status = status;
        this.email = email;
        this.role = role;
        this.plan = plan;
        this.storageLimit = storageLimit;
        this.storageUsed = storageUsed;
    }


    //Getter & Setter
    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public String getemail() { return email; }

    public void setemail(String email) { this.email = email; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public AccountRole getRole() { return role; }

    public void setRole(AccountRole role) { this.role = role; }

    public AccountPlan getPlan() { return plan; }

    public void setPlan(AccountPlan plan) { this.plan = plan; }

    public Long getStorageLimit() { return storageLimit; }

    public void setStorageLimit(Long storageLimit) { this.storageLimit = storageLimit; }

    public Long getStorageUsed() { return storageUsed; }
    public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }

    public AccountStatus getStatus() { return status; }

    public void setStatus(AccountStatus status) { this.status = status; }
}
