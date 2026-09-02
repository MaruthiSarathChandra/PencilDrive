package com.vanam.pencildrive.service;
import com.vanam.pencildrive.CustomException.GroupMemberAlreadyExistsException;
import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.dto.GroupMembersRequest;
import com.vanam.pencildrive.enums.AccountStatus;
import com.vanam.pencildrive.enums.GroupRole;
import com.vanam.pencildrive.repo.GroupMemberRepo;
import com.vanam.pencildrive.repo.LoginRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupMemberService {

    private static final Logger log = LoggerFactory.getLogger(GroupMemberService.class);
    private final GroupMemberRepo groupMemberRepo;
    private final LoginRepo loginRepo;

    public GroupMemberService(
            GroupMemberRepo groupMemberRepo,
            LoginRepo loginRepo
    ) {
        this.groupMemberRepo = groupMemberRepo;
        this.loginRepo = loginRepo;
    }



    /**
     * This File Handles GroupMembers CRUD Operations
     *
     *  Create                                ---> Check
     *  Delete                                --->
     *  Read                                  --->
     *  Update                                --->
     *
     *
     * */

    @Transactional
    public List<GroupMembers> create(
            List<GroupMembersRequest> groupMembersRequests, Groups group, User ownerObject) {

        if(groupMembersRequests == null || groupMembersRequests.isEmpty()) {
            return new ArrayList<GroupMembers>();
        }

        //<--------------------> |Group Members Verification| <------------------------>
        // 1. Extract & Create
        List<String> membersEmailsList =
                groupMembersRequests
                        .stream()
                        .map(GroupMembers -> GroupMembers.email())
                        .distinct().toList();

        // 2. Verify & Create
        Map<String, User> verifiedMembersHashMap =
                loginRepo
                        .findUserByEmailIdInAndStatus(
                                membersEmailsList,
                                AccountStatus.ACTIVE
                        ).stream()
                        .collect(
                                Collectors
                                        .toMap(
                                                User::getEmailId,
                                                user -> user
                                        ));


        HashSet<String> existingGroupMembers =
                new HashSet<>(groupMemberRepo.findEmailsByEmails(group, membersEmailsList));


        ////<--------------------> |Final Members List For Creation and Invitation| <------------------------>
        List<GroupMembers> pendingMembersToSave =
                    groupMembersRequests.stream()
                            .filter(Objects::nonNull)
                            .filter(dto -> dto.email() != null)
                            .filter(dto -> verifiedMembersHashMap.containsKey(dto.email()))
                            .filter(dto -> !existingGroupMembers.contains(dto.email()))
                            .map(dto -> GroupMembers.createGroupMember(
                                    verifiedMembersHashMap.get(dto.email().trim().toLowerCase(Locale.ROOT)),
                                    group,
                                    dto.role())).toList();

        if(pendingMembersToSave.isEmpty()) {
            log.error(" GroupMemberService Error : " +
                    "Line 37 : Failed to create List<?> membersToSave");
            throw new RuntimeException("SomeThing went Wrong");
        }


        // 2. Save them all to the database in one batch
        try {
            return groupMemberRepo.saveAllAndFlush(pendingMembersToSave);
        } catch (DataIntegrityViolationException e){
            throw new GroupMemberAlreadyExistsException(
                    "One or more users are already members of this group."
            );
        }catch (Exception e) {
            log.error("Failed to save entity objects" + e.toString() + e.getCause());
            throw new RuntimeException(e.toString());
        }
    }
}
