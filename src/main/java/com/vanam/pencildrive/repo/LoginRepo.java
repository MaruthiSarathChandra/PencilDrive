package com.vanam.pencildrive.repo;

import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.enums.AccountStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface LoginRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmailId(String emailId);
    Optional<User> findUserByEmailIdAndStatus(String emailId, AccountStatus status);
    List<User> findUserByEmailIdInAndStatus(List<String> emailIds, AccountStatus Status);

    @Query("""
        SELECT u.id
        FROM User u 
        WHERE u.emailId = ?1 
        """)
    Optional<Long> findIdByEmailId(
            @Param("emailId") String emailId
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
            WHERE u.emailId = : emailId AND u.storageUsed + :fileSize <= u.storageLimit
            """)
    Long[] findIdAndStorageUsedByEmailId(
            @Param("emailId") String emailId,
            @Param("fileSize") Long fileSize
    );

}
