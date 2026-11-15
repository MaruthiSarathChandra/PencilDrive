package com.vanam.pencildrive.domain;


import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.domain.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "files_groups",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_owner_name",
                        columnNames = {"owner_id","group_name"}
                )
        },
        indexes = {
                @Index(name = "idx_groups_owner", columnList = "owner_id"),
                @Index(name = "idx_groups_publicGroupId", columnList = "publicGroupId")
        }
)
public class Groups {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "public_group_id", nullable = false, unique = true)
    private UUID publicGroupId;


    protected Groups() {}

    public Groups(
            User owner,
            String groupName
    ) {
        this.owner = owner;
        this.groupName = groupName;
        this.publicGroupId = UUID.randomUUID();
    }

    public static Groups createGroup(
            User owner,
            String groupName
    ) {
        return new Groups(
                owner,
                groupName
        );
    }


    //Getters
    public Long getId() { return id; }

    public User getOwner() { return owner; }

    public String getGroupName() { return groupName; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public UUID getPublicGroupId() { return publicGroupId; }
}
