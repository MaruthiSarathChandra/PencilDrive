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
        * file
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
        private FilesMetadata file;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
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

        protected FileGroupPermissions() {}

        public FileGroupPermissions(
                FilesMetadata file,
                Groups groupId,
                Integer permissionBits,
                User createdBy
        ) {
                this.file = file;
                this.groupId = groupId;
                this.permissionBits = permissionBits;
                this.createdBy = createdBy;
        }

        public static FileGroupPermissions Create(
                FilesMetadata file,
                Groups groupId,
                Integer permissionBits,
                User createdBy
        ){
                return new FileGroupPermissions(
                        file,
                        groupId,
                        permissionBits,
                        createdBy
                );
        }








        //Getter
        public Long getId() { return id; }
        public FilesMetadata getfile() { return file; }
        public Groups getGroupId() { return groupId; }
        public Integer getPermissionBits() { return permissionBits; }
        public User getCreatedBy() { return createdBy; }
        public LocalDateTime getCreatedAt() { return createdAt; }
}
