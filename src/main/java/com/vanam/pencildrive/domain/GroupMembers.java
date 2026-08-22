package com.vanam.pencildrive.domain;


import com.vanam.pencildrive.enums.GroupRole;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.swing.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_members",
        indexes = {
                @Index(name = "idx_group_members_user_group_unique", columnList = "user_id,group_id", unique = true),
                @Index(name = "idx_group_members_group_user", columnList = "user_id")
        }
)
public class GroupMembers {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id", nullable = false)
        private User userId;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "group_id", nullable = false)
        private Groups groupId;

        @Column(name = "role", nullable = false)
        private GroupRole role;
        @CreationTimestamp
        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;


        public GroupMembers(
                User userId,
                Groups groupId,
                GroupRole role
        ) {
                this.userId = userId;
                this.groupId = groupId;
                this.role = role;
        }

        public static GroupMembers createGroupMember(
                User userId,
                Groups groupId,
                GroupRole role
        ) {
               return new GroupMembers(
                       userId, groupId, role
               );
        }


        //Getters
        public Long getId() { return id; }

        public User getUserId() { return userId; }

        public Groups getGroupId() { return groupId; }

        public GroupRole getRole() { return role; }

        public LocalDateTime getCreatedAt() { return createdAt; }
}
