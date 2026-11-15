package com.vanam.pencildrive.repo;

import com.vanam.pencildrive.domain.Groups;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.dto.ExistingPencilDriveUsers;
import com.vanam.pencildrive.dto.MemberClassification;
import com.vanam.pencildrive.enums.AccountStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
public interface LoginRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findUserByEmailAndStatus(String email, AccountStatus status);

    @Query("""
        SELECT u.id
        FROM User u
        WHERE u.email = ?1
        """)
    Optional<Long> findIdByEmail(
            @Param("email") String email
    );

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE User u " +
            "SET u.storageUsed = u.storageUsed + :fileSize " +
            "WHERE u.id = :id " +
            "AND :fileSize > 0 " +
            "AND u.storageUsed + :fileSize <= u.storageLimit")
    int reserveStorage(
            @Param("id") Long id,
            @Param("fileSize") Long fileSize
    );


    @Modifying
    @Query("""
            UPDATE User u
                SET u.storageUsed =
                    CASE
                        WHEN u.storageUsed >= :fileSize
                        THEN u.storageUsed - :fileSize
                        ELSE 0
                    END
                WHERE u.id = :id
            """)
    int releaseStorage(
            @Param("id") Long id,
            @Param("fileSize") Long fileSize
    );


    @Transactional
    @Query("""
            SELECT u.id, u.storageUsed
            FROM User u
            WHERE u.email = : email AND u.storageUsed + :fileSize <= u.storageLimit
            """)
    Long[] findIdAndStorageUsedByEmail(
            @Param("email") String email,
            @Param("fileSize") Long fileSize
    );







    //un-used
    @Query("""
            SELECT new com.vanam.pencildrive.dto.ExistingPencilDriveUsers(u.email, u)
            FROM User u
            WHERE u.email IN :emails
            AND u.status = com.vanam.pencildrive.enums.AccountStatus.ACTIVE
            """)
    List<ExistingPencilDriveUsers> findUserByEmailInAndStatus(
            @Param("emails") List<String> emails,
            @Param("status") AccountStatus status);




}
