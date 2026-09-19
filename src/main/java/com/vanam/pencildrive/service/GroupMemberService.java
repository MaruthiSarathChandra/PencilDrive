package com.vanam.pencildrive.service;
import com.vanam.pencildrive.CustomException.GroupMemberAlreadyExistsException;
import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.dto.GetGroupMembersResponse;
import com.vanam.pencildrive.dto.GroupMembersRequest;
import com.vanam.pencildrive.enums.AccountStatus;
import com.vanam.pencildrive.repo.GroupMemberRepo;
import com.vanam.pencildrive.repo.GroupsRepo;
import com.vanam.pencildrive.repo.LoginRepo;
import com.vanam.pencildrive.security.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupMemberService {

    private static final Logger log = LoggerFactory.getLogger(GroupMemberService.class);
    private final GroupMemberRepo groupMemberRepo;
    private final LoginRepo loginRepo;
    private final CurrentUserService currentUserService;
    private final GroupsRepo groupsRepo;

    public GroupMemberService(
            GroupMemberRepo groupMemberRepo,
            LoginRepo loginRepo,
            CurrentUserService currentUserService,
            GroupsRepo groupsRepo
    ) {
        this.groupMemberRepo = groupMemberRepo;
        this.loginRepo = loginRepo;
        this.currentUserService = currentUserService;
        this.groupsRepo = groupsRepo;
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
                new HashSet<>(groupMemberRepo.findEmailsByEmailsAndGroup(group, membersEmailsList));


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
            throw new RuntimeException("SomeThing Went Wrong");
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


    /**
     * Read Operation
     */
    @Transactional(readOnly = true)
    public GetGroupMembersResponse getGroupMembers(
            UUID publicGroupId, int page, int pageSize) {

        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }

        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        String user = currentUserService.getCurrentUserEmail();

        if(user == null) {
            log.error("User Object Cannot be Null able");
            return GetGroupMembersResponse.message("Something went wrong, Try Again Later");
        }

        Optional<Groups> group = groupsRepo.findByPublicGroupId(publicGroupId);
        if(group.isEmpty()) {
            return GetGroupMembersResponse.message("Something went wrong, Try Again Later");
        }

        if(!groupMemberRepo.existsByUserId_EmailIdAndGroupId(user, group.get())) {
            throw new RuntimeException("{User|groupId} No Data Found");
        }

        Pageable pageable = PageRequest.of(
                page,
                pageSize,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc( "id")
                )
        );

        return new GetGroupMembersResponse(
                "Success",
                groupMemberRepo.findEmailsByGroupId(group.get(), pageable));
    }
}
