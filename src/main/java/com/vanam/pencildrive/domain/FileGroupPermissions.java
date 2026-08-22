package com.vanam.pencildrive.domain;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "file_group_permissions",
        indexes = {
                @Index(name = "idx_fgp_file_group_unique", columnList = "file_id,group_id", unique = true),
                @Index(name = "idx_fgp_group_file", columnList = "group_id,file_id")
        }
)
public class FileGroupPermissions {


        /*
        * Id
        * FileId
        * GroupId
        * UserId
        * PermissionBits
        * UserId
        * CreatedAt
        *
        * */

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "file_id", nullable = false)
        private FilesMetadata fileId;

        @ManyToOne
        @JoinColumn(name = "group_id", nullable = false)
        private Groups groupId;

        @Column(name = "permission_bits", nullable = false)
        private Integer permissionBits;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "created_by", nullable = false)
        private User createdBy;

        @CreationTimestamp
        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;



        public FileGroupPermissions(
                FilesMetadata fileId,
                Groups groupId,
                Integer permissionBits,
                User createdBy
        ) {
                this.fileId = fileId;
                this.groupId = groupId;
                this.permissionBits = permissionBits;
                this.createdBy = createdBy;
        }

        public static FileGroupPermissions createdUploading(
                FilesMetadata fileId,
                Groups groupId,
                Integer permissionBits,
                User createdBy
        ){
                return new FileGroupPermissions(
                        fileId,
                        groupId,
                        permissionBits,
                        createdBy
                );
        }








        //Getter
        public Long getId() { return id; }
        public FilesMetadata getFileId() { return fileId; }
        public Groups getGroupId() { return groupId; }
        public Integer getPermissionBits() { return permissionBits; }
        public User getCreatedBy() { return createdBy; }
        public LocalDateTime getCreatedAt() { return createdAt; }
}
