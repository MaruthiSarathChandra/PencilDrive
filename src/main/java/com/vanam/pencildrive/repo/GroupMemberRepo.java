package com.vanam.pencildrive.repo;
import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface GroupMemberRepo
        extends JpaRepository<GroupMembers, Long> {


    @Query("""
        SELECT gm.userId.emailId 
        FROM GroupMembers gm 
        WHERE gm.groupId = :group
        AND gm.userId.emailId IN :emails
        """)
    List<String> findEmailsByEmails(@Param("group") Groups group, List<String> emails);
}
