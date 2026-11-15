package com.vanam.pencildrive.repo;
import com.fasterxml.jackson.core.JsonParser;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.dto.GetGroups;
import com.vanam.pencildrive.enums.GroupRole;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface GroupsRepo extends JpaRepository<Groups, Long> {

    boolean existsByOwner_IdAndGroupName(Long ownerId, String groupName);

    Optional<Groups> findByPublicGroupId(UUID publicGroupId);



    @Query("""
            SELECT CASE
                WHEN g.owner.email = :email THEN com.vanam.pencildrive.enums.GroupRole.OWNER
                ELSE gm.role
            END
            FROM Groups g
            JOIN GroupMembers gm ON gm.groupId = g AND gm.userId.email = :email
            WHERE g.publicGroupId = :publicGroupId
            AND (g.owner.email = :email OR gm.userId.email = :email)
            """)
    Optional<GroupRole> isUserOwnerOrMember(
            @Param("publicGroupId") UUID publicGroupId,
            @Param("email")String email);



    @Query("""
    SELECT g, gm
    FROM Groups g
    LEFT JOIN GroupMembers gm
        ON gm.groupId = g
    WHERE g.owner.email = :email
       OR EXISTS (
           SELECT 1
           FROM GroupMembers member
           WHERE member.groupId = g
             AND member.userId.email = :email
       )
    """)
    List<Object[]> getGroupsJson(@Param("email") String email);

}
