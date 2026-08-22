package com.vanam.pencildrive.service;
import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.dto.CreateGroupRequest;
import com.vanam.pencildrive.dto.GroupResponse;
import com.vanam.pencildrive.dto.MessageResponse;
import com.vanam.pencildrive.repo.FileMetadataRepo;
import com.vanam.pencildrive.repo.GroupsRepo;
import com.vanam.pencildrive.repo.LoginRepo;
import com.vanam.pencildrive.security.CurrentUserService;
import jakarta.annotation.Nullable;
import jakarta.persistence.Id;
import org.hibernate.jdbc.Expectation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;




/**
     * Group service.
     *
     * Handles group-related business logic.
     * This includes creating groups, adding members, removing members,
     * changing member roles, deleting groups, and fetching user groups.
     *
     * Group membership controls access to files shared with a group.
     * Removing a user from a group should only remove the group_members record.
     * File group permission records should not be deleted when one member is removed.
     */

@Service
public class GroupService {


    /**
     * create/update
     *
     * delete/remove
     * */
    private static final Logger log =
            LoggerFactory.getLogger(GroupService.class);
    private final CurrentUserService currentUserService;
    private final LoginRepo loginRepo;
    private final GroupsRepo groupsRepo;
    private final GroupMemberService groupMemberService;
    private final FileMetadataRepo fileMetadataRepo;
    private final PermissionService permissionService;
    private final TransactionTemplate transactionTemplate;
    public GroupService(
            CurrentUserService currentUserService,
            LoginRepo loginRepo,
            GroupsRepo groupsRepo,
            GroupMemberService groupMemberService,
            FileMetadataRepo fileMetadataRepo,
            PermissionService permissionService,
            TransactionTemplate transactionTemplate
    ) {
        this.currentUserService = currentUserService;
        this.loginRepo = loginRepo;
        this.groupsRepo = groupsRepo;
        this.groupMemberService = groupMemberService;
        this.fileMetadataRepo = fileMetadataRepo;
        this.permissionService = permissionService;
        this.transactionTemplate = transactionTemplate;
    }

    public GroupResponse createGroup(CreateGroupRequest request) {


        //<--------------------> | 1. Owner Verification | <------------------------>
        Optional<User> ownerId =
                currentUserService.requireCurrentUserObject();

        if(ownerId.isEmpty()) {
            log.error("Querying Error: User not for email" + currentUserService.getCurrentUserEmail());
            return GroupResponse.message("something went wrong while creating group");
        }



        GroupCreationResult creationResult = transactionTemplate.execute( status -> {
            try {

                /** 1. Creating Groups Object , State Pending**/
                Groups pendingGroup = Groups.createGroup
                        (ownerId.get(), request.groupName());


                return new GroupCreationResult(
                        /** 2.Save Pending Group and Flush */
                        groupsRepo.save(pendingGroup),
                        /** 3. Calling for Creation Of Group Members */
                        groupMemberService.create(request.members(), pendingGroup)
                );
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("Transaction failed, rolling back Group and Members: {}", e.getMessage());
                throw new RuntimeException("Failed to create group and members", e);
            }
        });


        /** 4. Calling for Creation Of Group Members */
        if(request.file() != null) {
            if(permissionService.addFile(
                    creationResult.group,
                    request.file(),
                    ownerId.get()) == true) {

                return new GroupResponse("Success",
                        creationResult.group,
                        creationResult.members,
                        request.file());
            }
        }
        return GroupResponse
                .createGroupWithGroupMembers(
                        "Success",
                        creationResult.group,
                        creationResult.members
                );
    }

    private record GroupCreationResult(Groups group, List<GroupMembers> members) {}
}
