package com.vanam.pencildrive.service;
import com.google.common.collect.Lists;
import com.vanam.pencildrive.CustomException.GroupMemberAlreadyExistsException;
import com.vanam.pencildrive.CustomException.TooManyMembersException;
import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.dto.*;
import com.vanam.pencildrive.enums.AccountStatus;
import com.vanam.pencildrive.enums.GroupRole;
import com.vanam.pencildrive.events.InvitationsRequested;
import com.vanam.pencildrive.events.listener.InvitationEventListener;
import com.vanam.pencildrive.repo.GroupMemberRepo;
import com.vanam.pencildrive.repo.GroupsRepo;
import com.vanam.pencildrive.repo.LoginRepo;
import com.vanam.pencildrive.security.CurrentUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import javax.swing.*;
import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class GroupMemberService {

    private static final Logger log = LoggerFactory.getLogger(GroupMemberService.class);
    private final GroupMemberRepo groupMemberRepo;
    private final LoginRepo loginRepo;
    private final CurrentUserService currentUserService;
    private final GroupsRepo groupsRepo;
    private static final int CHUNK = 500;
    private static final int MAX_REQUEST = 5_000;
    private final ApplicationEventPublisher events;

    @PersistenceContext
    private EntityManager entityManager;

    public GroupMemberService(
            GroupMemberRepo groupMemberRepo,
            LoginRepo loginRepo,
            CurrentUserService currentUserService,
            GroupsRepo groupsRepo,
            ApplicationEventPublisher events
    ) {
        this.groupMemberRepo = groupMemberRepo;
        this.loginRepo = loginRepo;
        this.currentUserService = currentUserService;
        this.groupsRepo = groupsRepo;
        this.events = events;
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
            List<GroupMembersRequest> groupMembersRequests, Groups group) {

        if(groupMembersRequests == null || groupMembersRequests.isEmpty()) {
            return new ArrayList<GroupMembers>();
        }

        if(groupMembersRequests.size() > MAX_REQUEST) {
            throw new TooManyMembersException( "TooManyMembersException Found");
        }

        //<--------------------> | 1. Group Members Verification | <------------------------>
        // 1. Extract & Create

        Map<String, GroupRole> requestMap = groupMembersRequests.stream()
                .filter(req -> req != null && req.email() != null && !req.email().isBlank())
                .collect(Collectors
                        .toMap(
                            req -> req.email().trim().toLowerCase(Locale.ROOT),
                            GroupMembersRequest::role,
                            (existing, replacement) -> existing, // prevents duplicate values like putIfAbsent in-built method in java Map interface
                            LinkedHashMap::new
                ));


        //<--------------------> | 2. COMMENTS NEED TO BE UPDATED | <------------------------>
        List<String> membersEmailsList = List.copyOf(requestMap.keySet());
        Set<String> toInvite = new HashSet<>(requestMap.keySet());
        List<GroupMembers> written = new ArrayList<>();
        List<GroupMembers> pendingMembersToSavee = new ArrayList<>();


        for (List<String> chunk : Lists.partition(membersEmailsList, CHUNK)) {

            List<MemberClassification> rows = groupMemberRepo.classify(group, chunk);

            List<UpsertRow> upserts = new ArrayList<>();

            rows.stream().forEach(
                    member -> {
                        String email =member.getEmail().trim().toLowerCase(Locale.ROOT);

                        System.out.println(member + "groupmember" + member.getGroupMember() + "" + member.getUser());

                        if(member.getGroupMember() != null && member.getGroupMember().getRole() == GroupRole.DELETED) {
                            written.add(
                                    groupMemberRepo.saveAndFlush(GroupMembers.upsertRole(
                                            member.getGroupMember(),
                                            requestMap.get(member.getUser().getEmail())
                                    )));
                        } else if (member.getGroupMember() == null && member.getUser() != null) {
                            pendingMembersToSavee.add(GroupMembers.createGroupMember(
                                    member.getUser(),
                                    group,
                                    requestMap.get(member.getEmail())));
                            toInvite.remove(email);
                        } else {
                            written.add(member.getGroupMember());
                        }
                    }
            );
        }

        if(!toInvite.isEmpty()) {
            events.publishEvent(new InvitationsRequested(group.getId(), List.copyOf(toInvite)));
        }

        if(pendingMembersToSavee.isEmpty() && written.isEmpty()) {
            log.error(" GroupMemberService Error : " +
                    "Line 37 : Failed to create List<?> membersToSave");
            throw new RuntimeException("SomeThing Went Wrong");
        }

        try {

            written.addAll(groupMemberRepo.saveAllAndFlush(pendingMembersToSavee));
            return written;
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

        //<--------------------> |Validate Required Page| <------------------------>
        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative");
        }

        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100"
            );
        }

        //<--------------------> |Get Request User Credentials| <------------------------>
        String user =
                currentUserService.getCurrentUserEmail();

        if(user == null) {
            log.error("User Object Cannot be Null able");
            return GetGroupMembersResponse.message("Something went wrong, Try Again Later");
        }

        //<--------------------> |Validating Both Group, Requested User Exists in Requested Group| <------------------------>
        Optional<Groups> group = groupsRepo.findByPublicGroupId(publicGroupId);
        if(group.isEmpty()) {
            return GetGroupMembersResponse.message("Something went wrong, Try Again Later");
        }

        if(!groupMemberRepo.existsByUserId_EmailAndGroupId(user, group.get())) {
            throw new RuntimeException("{User|groupId} No Data Found");
        }

        //<--------------------> |Defining Page Outline to inject in Sql Query| <------------------------>
        Pageable pageable = PageRequest.of(
                page,
                pageSize,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc( "id")
                )
        );

        //<--------------------> | Returning Page with Requested | <------------------------>
        return new GetGroupMembersResponse(
                "Success",
                groupMemberRepo.findEmailsByGroupId(group.get(), pageable));
    }


    /**
     * Delete Operation
     *  -> check groupmembers dto
     *  -> check both group, user exists in group
     *  -> Does user has valid role to authernticate removing member
     *  -> perform operation and return success message
     * **/
    @Transactional
    public void removeMemeber(DeleteGroupMembers deleteGroupMembers){

        UUID publicGroupId = deleteGroupMembers.publicGroupId();
        List<GroupMembersRequest> gmRequest = deleteGroupMembers.groupMembersRequest() ;

        //<--------------------> | Validate GroupMemberRequest DTO (Data-Transfer-Object)| <------------------------>
        if(gmRequest == null || gmRequest.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Payload cannot be empty");
        }
        //<--------------------> | Requested Users Credentials | <------------------------>
        String requestedUser =
                currentUserService.getCurrentUserEmail();

        //<--------------------> | Validating User Authorized to perform Transaction Based on his Role | <------------------------>
        GroupRole isAdminOrOwner =
                groupsRepo.isUserOwnerOrMember(publicGroupId, requestedUser).get();

        if(!EnumSet.of(GroupRole.ADMIN, GroupRole.OWNER).contains(isAdminOrOwner)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User is not authorized to remove group members");
        }

        //<--------------------> | Safe & Sanitized Extraction of Targeted Member | <------------------------>
        List<String> groupMemberEmail =
                gmRequest.stream()
                        .filter(Objects::nonNull)
                        .map(GroupMembersRequest::email)
                        .filter(Objects::nonNull)
                        .map(email ->
                                email.trim()
                                        .toLowerCase(Locale.ROOT))
                        .filter(email -> !email.isBlank())
                        .distinct().toList();

        //<--------------------> | Verifying Operational Size | <-------------------->
        if (groupMemberEmail.size() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum 100 members may be removed at once"
            );
        }

        //<--------------------> | WARNING: Extraction of Reference Ghost Objects| <------------------------>
        //<------> We Need to return something different if GroupMembers are actually members<------->
        User anonymousUser =
                entityManager.getReference(User.class, 1L);
        Groups anonymousGroups =
                entityManager.getReference(Groups.class, 1L);

        //<--------------------> | Performing removeMember Transaction | <------------------------>
        int state =
                groupMemberRepo.anonymizeGroupMember(
                        anonymousUser,
                        anonymousGroups,
                        groupMemberEmail,
                        publicGroupId);

        if(state == 0) {
            log.error("Failed to remove member. Target {} not found in group {}",
                    gmRequest, publicGroupId);

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User is not a member of this group."
            );
        }
    }



    /**
     * Secured wrapper for injecting new members into an already existing group.
     *
     * This method acts as the authorization gatekeeper before delegating to the core
     * create() persistence logic. It verifies the requester's identity and power level
     * across the split-table hierarchy, ensuring strict Role-Based Access Control (RBAC).
     *
     * Security Rules Enforced:
     * 1. Intruder Defense: Requester must be recognized in the group's hierarchy.
     * 2. Privilege Check: Only users with OWNER or ADMIN roles can authorize additions.
     *
     * @param "members" DTO containing the target group's public UUID and the list of new members.
     * @return GetGroupMembersResponse containing the refreshed, paginated member list.
     * @throws "ResponseStatusException" (403) if the requester is unauthorized or an intruder.
     */
    @Transactional
    public GetGroupMembersResponse addMemeberByGroup(AddMemberToExistingGroup members){

        String requesterEmail = currentUserService.getCurrentUserEmail();

        // 1. Unified Security Check: Inspect both Groups and GroupMembers tables simultaneously
        GroupRole requestRole = groupMemberRepo.
                findUnifiedUserRole(requesterEmail,
                        members.publicGroupId(),
                        GroupRole.OWNER
                ).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Intruder Blocked: You are not a member of this group."));

        // 2. Hierarchy Enforcement: Restrict action to top-level management
        if(requestRole != GroupRole.OWNER && requestRole != GroupRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Unauthorized: Only Admins and Owners can add new members.");
        }

        // 3. Delegate to Core Creation Logic
        // Note: Consider replacing .get() with .orElseThrow() to prevent 500 errors on invalid UUIDs
        try { create(members.members(),
                groupsRepo.findByPublicGroupId(members.publicGroupId()).get()
        );
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // 4. Return the refreshed state of the group
        return getGroupMembers(members.publicGroupId(), 0, 100);
    }


    @Transactional
    public int reactivateMembers(
       Groups group,
       List<UpsertRow> upsertRows,
       List<GroupMembers> written
    ) {
        Map<GroupRole, List<Long>> groupedByRole =
                upsertRows.stream()
                        .collect(Collectors.groupingBy(
                                UpsertRow::role,
                                Collectors.mapping(
                                        UpsertRow::userId,
                                        Collectors.toList()
                                )));

        int updatedCount = 0;
        for (var entry : groupedByRole.entrySet()) {
            updatedCount += groupMemberRepo.reactivateUser(
                    group,
                    entry.getKey(),
                    entry.getValue()
            );
        }
        return updatedCount;
    }
}
