package com.vanam.pencildrive.dto;
import com.vanam.pencildrive.enums.AccountPlan;
import com.vanam.pencildrive.enums.AccountRole;
import com.vanam.pencildrive.enums.AccountStatus;
import jakarta.annotation.Nullable;

public class ProfileResponse {


    private String userName;
    private String emailId;
    private String message;
    private AccountRole role;
    private AccountPlan plan;
    private Long storageLimit;
    private Long storageUsed;
    private AccountStatus status;


    public ProfileResponse(@Nullable String message, @Nullable String userName, @Nullable AccountStatus status,
                           @Nullable String emailId, @Nullable AccountRole role,
                           @Nullable AccountPlan plan, @Nullable Long storageLimit, @Nullable Long storageUsed) {
        this.message = message;
        this.userName = userName;
        this.status = status;
        this.emailId = emailId;
        this.role = role;
        this.plan = plan;
        this.storageLimit = storageLimit;
        this.storageUsed = storageUsed;
    }


    //Getter & Setter
    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public String getEmailId() { return emailId; }

    public void setEmailId(String emailId) { this.emailId = emailId; }

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
