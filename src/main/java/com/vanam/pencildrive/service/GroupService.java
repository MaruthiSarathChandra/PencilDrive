package com.vanam.pencildrive.service;
import com.vanam.pencildrive.CustomException.GroupAlreadyExistsException;
import com.vanam.pencildrive.CustomException.GroupMemberAlreadyExistsException;
import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.dto.*;
import com.vanam.pencildrive.enums.GroupRole;
import com.vanam.pencildrive.repo.FileMetadataRepo;
import com.vanam.pencildrive.repo.GroupsRepo;
import com.vanam.pencildrive.repo.LoginRepo;
import com.vanam.pencildrive.security.CurrentUserService;
import jakarta.annotation.Nullable;
import jakarta.persistence.Id;
import org.hibernate.jdbc.Expectation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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

    public CreateGroupResponse createGroup(CreateGroupRequest request) {

        //<--------------------> | 1. Owner Verification | <------------------------>
        Optional<User> owner =
                currentUserService.requireCurrentUserObject();

        if(owner.isEmpty()) {
            log.error("Querying Error: User not for email" + currentUserService.getCurrentUserEmail());
            return CreateGroupResponse.message("something went wrong while creating group");
        }


        if(groupsRepo.existsByOwner_IdAndGroupName(owner.get().getId(), request.groupName()) == true) {
            return CreateGroupResponse.message("GroupName Already Exists");
        }


        GroupCreationResult creationResult = transactionTemplate.execute( status -> {
            try {

                /** 1. Creating Groups Object , State Pending**/
                Groups pendingGroup = Groups.createGroup
                        (owner.get(), request.groupName());

                return new GroupCreationResult(
                        /** 2.Save Pending Group and Flush */
                        groupsRepo.saveAndFlush(pendingGroup),
                        /** 3. Calling for Creation Of Group Members */
                        groupMemberService.create(request.members(), pendingGroup, owner.get())
                );
            } catch (GroupMemberAlreadyExistsException e) {
                throw e;

            }catch (RuntimeException e) {
                if(e instanceof DataIntegrityViolationException) {
                    throw new GroupAlreadyExistsException("Group Already Exists");
                }
                status.setRollbackOnly();
                log.error("Transaction failed, rolling back Group and Members: {}", e.getMessage());
                throw new RuntimeException("Failed to create group and members", e);
            }
        });


        /** 4. Calling for Creation Of Group Members */
        if(request.file() != null) {
            if(permissionService.shareFileWithGroup(
                    creationResult.group,
                    request.file(),
                    owner.get()) == true) {

                return new CreateGroupResponse("Success",
                        new GroupResponse(creationResult.group.getPublicGroupId(), creationResult.group.getGroupName()),
                        creationResult
                                .members.stream()
                                .map(member ->
                                        new GroupMembersResponse(
                                                        member.getUserId().getEmailId(),
                                                        member.getRole())
                                ).toList(),
                        request.file());
            }
        }
        return CreateGroupResponse
                .createGroupWithGroupMembers(
                        "Success",
                        new GroupResponse(creationResult.group.getPublicGroupId(), creationResult.group.getGroupName()),
                        creationResult.members.stream()
                                .map(member ->
                                        new GroupMembersResponse(
                                                member.getUserId().getEmailId(),
                                                member.getRole())).toList()
                );
    }

    private record GroupCreationResult(Groups group, List<GroupMembers> members) {}
}
