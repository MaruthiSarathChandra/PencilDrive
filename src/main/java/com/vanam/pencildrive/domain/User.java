package com.vanam.pencildrive.domain;


import com.vanam.pencildrive.enums.AccountPlan;
import com.vanam.pencildrive.enums.AccountRole;
import com.vanam.pencildrive.enums.AccountStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "emailId", nullable = false, length = 100)
    private String emailId;
    @Column(name = "password", nullable = false, length = 100)
    private String password;
    @Column(name = "name", nullable = false, length=20)
    private String username;
    @Column(name = "storage_used_bytes", nullable = false)
    private Long storageUsed;
    @Column(name = "storage_limit", nullable = false)
    private Long storageLimit;

    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @Column(name = "role", nullable = false)
    private AccountRole role;
    @Column(name = "plan", nullable = false)
    private AccountPlan plan;








    // Getter & Setter
    public String getEmailId() {
        return emailId;
    }

    public String getPassword() {
        return password;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public void setPassword(String password) { this.password = password; }

    public void setEmailId(String emailId) { this.emailId = emailId; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public Long getStorageUsed() { return storageUsed; }

    public void setStorageUsed(Long storage_used) { this.storageUsed = storage_used; }

    public Long getStorageLimit() { return storageLimit; }

    public void setStorageLimit(Long storageLimit) { this.storageLimit = storageLimit; }

    public AccountStatus getStatus() { return status; }

    public void setStatus(AccountStatus status) { this.status = status; }

    public AccountRole getRole() { return role; }

    public void setRole(AccountRole role) { this.role = role; }

    public AccountPlan getPlan() { return plan; }

    public void setPlan(AccountPlan plan) { this.plan = plan; }
}
