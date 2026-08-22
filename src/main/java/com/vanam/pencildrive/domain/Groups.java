package com.vanam.pencildrive.domain;


import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.domain.User;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.swing.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "files_groups",
        indexes = {
                @Index(name = "idx_groups_owner", columnList = "owner_id")
        }
)
public class Groups {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User userId;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Groups(
            User userId,
            String groupName
    ) {
        this.userId = userId;
        this.groupName = groupName;
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

    public User getUserId() { return userId; }

    public String getGroupName() { return groupName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
