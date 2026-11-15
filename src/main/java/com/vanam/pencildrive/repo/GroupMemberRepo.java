package com.vanam.pencildrive.repo;
import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.dto.ExistingGroupMember;
import com.vanam.pencildrive.dto.GroupMembersResponse;
import com.vanam.pencildrive.dto.MemberClassification;
import com.vanam.pencildrive.dto.UpsertRow;
import com.vanam.pencildrive.enums.GroupRole;
import jakarta.transaction.UserTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Repository
public interface GroupMemberRepo
        extends JpaRepository<GroupMembers, Long> {




    Boolean existsByUserId_EmailAndGroupId(String email, Groups groupId);

    @Query("""
            SELECT new com.vanam.pencildrive.dto.GroupMembersResponse(gm.userId.email, gm.role)
            FROM GroupMembers gm
            WHERE gm.groupId = :group
            AND gm.role != com.vanam.pencildrive.enums.GroupRole.DELETED
            """)
    Slice<GroupMembersResponse> findEmailsByGroupId(@Param("group") Groups group, Pageable pageable);


    @Transactional
    @Modifying
    @Query(""" 
            UPDATE GroupMembers gm
            SET gm.role = GroupRole.DELETED
            WHERE gm.groupId.publicGroupId = :group
            AND gm.userId.email IN :emails
            AND gm.role != GroupRole.OWNER
            """)
    int anonymizeGroupMember(
            @Param("userId") User userId,
            @Param("groupId") Groups groupId,      /*gm.userId = :userId,  gm.groupId = :groupId,  ---> TombStone / Ghost-Record */
            @Param("emails") List<String> emails,
            @Param("group") UUID group);



    /**
     * Solves the "Split-Table Problem" by resolving a user's role in a single database hit.
     *
     * Architecture Context: In PencilDrive, the Group Owner is stored in the 'Groups' table,
     * while Admins and Members are stored in the 'GroupMembers' join table.
     *
     * This query uses a LEFT JOIN and a CASE statement to evaluate both tables simultaneously.
     * If the email matches the Groups table, it dynamically intercepts and returns the OWNER enum.
     * Otherwise, it returns the role found in the GroupMembers table. This prevents the "Owner Lockout"
     * bug and cuts database load in half by eliminating sequential table checks.
     */
    @Query("""
            SELECT CASE
                WHEN g.owner.email = :email THEN :ownerRole
                ELSE gm.role
            END
            FROM Groups g
            LEFT JOIN GroupMembers gm ON gm.groupId = g AND gm.userId.email = :email
            WHERE g.publicGroupId = :publicGroupId
            AND (g.owner.email = :email OR gm.userId.email = :email)
            """)
    Optional<GroupRole> findUnifiedUserRole(
            @Param("email") String email,
            @Param("publicGroupId") UUID publicGroupId,
            @Param("ownerRole") GroupRole ownerRole
    );



    @Modifying
    @Query("""
            UPDATE GroupMembers gm
            SET gm.role = :role
            WHERE gm.groupId = :group
            AND gm.userId IN :upserts
            """)
    int reactivateUser(
            @Param("groupId") Groups group,
            @Param("role") GroupRole role,
            @Param("upserts") List<Long> upsertRows
    );


    @Query("""
            SELECT u.email AS email, u AS user, gm AS groupMember
            FROM User u
            LEFT JOIN GroupMembers gm
            ON gm.userId = u AND gm.groupId = :group
            WHERE u.email IN :emails AND u.status = com.vanam.pencildrive.enums.AccountStatus.ACTIVE
            """)
    List<MemberClassification> classify(
            @Param("group") Groups group,
            @Param("emails") Collection<String> emails);













    //un-used
    @Query("""
        SELECT new com.vanam.pencildrive.dto.ExistingGroupMember(
            gm.userId.email,
            gm.role
        )
        FROM GroupMembers gm
        WHERE gm.groupId = :group
        AND gm.userId.email IN :emails
        """)
    List<ExistingGroupMember> findEmailsByEmailsAndGroup(
            @Param("group") Groups group,
            @Param("emails") List<String> emails);
}
