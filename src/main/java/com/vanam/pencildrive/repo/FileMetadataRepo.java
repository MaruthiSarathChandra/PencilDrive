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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Repository
public interface FileMetadataRepo extends JpaRepository<FilesMetadata, Long> {


    Slice<FilesMetadata> findByOwner_IdAndStatus(
            Long ownerId,
            FileStatus status,
            Pageable pageable
    );

    Optional<FilesMetadata> findById(Long fileId);

    Optional<FilesMetadata> findByOwnerIdAndIdAndStatus(FilesMetadata filesMetadata, User ownerId, FileStatus fileStatus);



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
