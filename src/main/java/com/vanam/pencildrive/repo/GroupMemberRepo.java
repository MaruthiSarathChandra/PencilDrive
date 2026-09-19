package com.vanam.pencildrive.repo;
import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.dto.GroupMembersResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;



@Repository
public interface GroupMemberRepo
        extends JpaRepository<GroupMembers, Long> {


    @Query("""
        SELECT gm.userId.emailId 
        FROM GroupMembers gm 
        WHERE gm.groupId = :group
        AND gm.userId.emailId IN :emails
        """)
    List<String> findEmailsByEmailsAndGroup(@Param("group") Groups group, List<String> emails);


    Boolean existsByUserId_EmailIdAndGroupId(String userId, Groups groupId);


    @Query("""
            SELECT new com.vanam.pencildrive.dto.GroupMembersResponse(gm.userId.emailId, gm.role)
            FROM GroupMembers gm
            WHERE gm.groupId = :group
            """)
    Slice<GroupMembersResponse> findEmailsByGroupId(@Param("group") Groups group, Pageable pageable);

}
