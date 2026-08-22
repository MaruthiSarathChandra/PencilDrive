package com.vanam.pencildrive.domain;


import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.domain.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "FilesPermissions",
        indexes = {
                @Index(name = "idx_perm_file_shared_unique", columnList = "file_id, shared_with_user", unique = true),
                @Index(name = "idx_perm_shared_file", columnList = "shared_with_user,file_id")
        }
)
public class FilesPermissions {

    /*
    * ID
    * file_id (FilesMetadata fileId)
    * created_by(UserRepository userId)
    * shared_with_user (UserRepository shared_with_user);
    * created_at
    * */


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private FilesMetadata fileId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_with_user", nullable = false)
    private User sharedWithUser;

    @Column(name = "permission_bits", nullable = false)
    private Integer permissionBits;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;




    //Getter And Setter

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public FilesMetadata getFileId() { return fileId; }

    public void setFileId(FilesMetadata fileId) { this.fileId = fileId; }

    public User getUserId() { return userId; }

    public void setUserId(User userId) { this.userId = userId; }

    public User getSharedWithUser() { return sharedWithUser; }

    public void setSharedWithUser(User sharedWithUser) { this.sharedWithUser = sharedWithUser; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getPermissionBits() { return permissionBits; }

    public void setPermissionBits(Integer permissionBits) { this.permissionBits = permissionBits; }
}
