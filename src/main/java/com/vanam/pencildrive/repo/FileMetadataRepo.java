package com.vanam.pencildrive.repo;
import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.enums.FileStatus;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface FileMetadataRepo extends JpaRepository<FilesMetadata, Long> {


    Slice<FilesMetadata> findByOwner_IdAndStatus(
            Long ownerId,
            FileStatus status,
            Pageable pageable
    );

    Optional<FilesMetadata> findById(Long fileId);



    /**findVerifiedFile == findByIdAndOwnerIdAndStatus*/
    @Query("""
            SELECT f FROM FilesMetadata f
            WHERE f.owner = :owner
            AND f.id = :fileId
            AND f.status = :status
            """)
    Optional<FilesMetadata> findVerifiedFile(
            @Param("owner") User owner,
            @Param("fileId") Long fileId,
            @Param("status") FileStatus status
    );
    Optional<FilesMetadata> findByPublicIdAndOwnerIdAndStatus(UUID publicId, Long ownerId, FileStatus status);



    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE FilesMetadata f
       SET f.status = :newStatus
     WHERE f.id = :fileId
       AND f.status = :oldStatus""")
    int transitionFileStatus(
            @Param("oldStatus") FileStatus oldStatus,
            @Param("fileId") Long fileId,
            @Param("newStatus") FileStatus newStatus
    );

}
