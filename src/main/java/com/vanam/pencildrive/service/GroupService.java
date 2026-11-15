package com.vanam.pencildrive.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanam.pencildrive.CustomException.GroupAlreadyExistsException;
import com.vanam.pencildrive.CustomException.GroupMemberAlreadyExistsException;
import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.dto.*;
import com.vanam.pencildrive.repo.FileMetadataRepo;
import com.vanam.pencildrive.repo.GroupsRepo;
import com.vanam.pencildrive.repo.LoginRepo;
import com.vanam.pencildrive.security.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

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
    private final ObjectMapper objectMapper;
    public GroupService(
            CurrentUserService currentUserService,
            LoginRepo loginRepo,
            GroupsRepo groupsRepo,
            GroupMemberService groupMemberService,
            FileMetadataRepo fileMetadataRepo,
            PermissionService permissionService,
            TransactionTemplate transactionTemplate,
            ObjectMapper objectMapper
    ) {
        this.currentUserService = currentUserService;
        this.loginRepo = loginRepo;
        this.groupsRepo = groupsRepo;
        this.groupMemberService = groupMemberService;
        this.fileMetadataRepo = fileMetadataRepo;
        this.permissionService = permissionService;
        this.transactionTemplate = transactionTemplate;
        this.objectMapper = objectMapper;
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
                        groupMemberService.create(request.members(), pendingGroup)
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
                                                        member.getUserId().getEmail(),
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
                                                member.getUserId().getEmail(),
                                                member.getRole())).toList()
                );
    }

    public List<GetGroups> getGroups() {

        String request = currentUserService.getCurrentUserEmail();

        if(request == null) {
            log.error("<------ Found Current User email as null -> GroupService.java , line: 156 ----->");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Intruder Blocked: You are not a member of this group.");
        }
        try {
            Map<UUID, GetGroups> getGroupsResponse = new HashMap<>();

            groupsRepo.getGroupsJson(request).forEach(objects -> {
               Groups group = (Groups) objects[0];
               GroupMembers groupMembers = (GroupMembers) objects[1];

               UUID publicGroupId = group.getPublicGroupId();

               GetGroups dto = getGroupsResponse.computeIfAbsent(
                       publicGroupId,
                       id -> new GetGroups(
                                   publicGroupId,
                                   group.getGroupName(),
                                   new ArrayList<>()
                           )
               );

               if(groupMembers != null) {
                   dto.groupMembersResponse().add(
                           new GroupMembersResponse(groupMembers.getUserId().getEmail(),
                                   groupMembers.getRole())
                   );
               }
            });
            return new ArrayList<>(getGroupsResponse.values());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to deserialize groups", e
            );
        }
    }



    private record GroupCreationResult(Groups group, List<GroupMembers> members) {}
}
