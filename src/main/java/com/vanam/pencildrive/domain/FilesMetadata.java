package com.vanam.pencildrive.domain;
import com.vanam.pencildrive.enums.FileStatus;
import jakarta.persistence.*;
import jdk.jshell.Snippet;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DialectOverride;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(
        name = "files",
        indexes = {
                @Index(name = "idx_files_owner_status_created", columnList = "owner_id,status,created_at"),
                @Index(name = "idx_files_name", columnList = "file_name"),
                @Index(name = "idx_files_storage_key", columnList = "storage_key", unique = true)
        },
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_files_storage_key",
                columnNames = "storage_key"
        )
    }
)
public class FilesMetadata {

    /* id
     * owner_id
     * file_name
     * original_name
     * content_type
     * size_bytes
     * storage_key
     * checksum
     * status
     * created_at
     * updated_at
     */


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    @Column(name = "file_name", nullable = false)
    private String fileName;                            //Changed from name
    @Column(name = "content_type", nullable = false)
    private String contentType;
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;
    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;
    @Column(name = "checksum", length = 64)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FileStatus status;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version                                           // new update version
    @Column(name = "version", nullable = false)        // new update version
    private Long version;                              // new update version

    protected FilesMetadata() {
    }

    public FilesMetadata(
            User owner,
            String fileName,                    //Changed from name
            String contentType,
            Long sizeBytes,
            String storageKey ) {
        this.owner = owner;
        this.fileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.storageKey = storageKey;
        this.status = FileStatus.UPLOADING;
        this.publicId = UUID.randomUUID();
    }

    public static FilesMetadata createUploading(
            User owner,
            String originalName,
            String contentType,
            long sizeBytes,
            String storageKey
    ) {
        return new FilesMetadata(
                owner,
                originalName,
                contentType,
                sizeBytes,
                storageKey
        );
    }

    public void markReady(String checksum) {
        if(status != FileStatus.UPLOADING &&
        status != FileStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Cannot move file from " + status + "to READY"
            );
        }

        this.checksum = checksum;
        this.status = FileStatus.READY;
        System.out.println(this.status);
    }

    public void markProcessing() {
        if(status != FileStatus.UPLOADING) {
            throw new IllegalStateException(
                    "Only an UPLOADING file can enter PROCESSING"
            );
        }
        this.status = FileStatus.PROCESSING;
    }

    public void markFailed() {
        if(status != FileStatus.READY) {
            this.status = FileStatus.FAILED;
        }
    }

    public void markDelete() {
        if(status != FileStatus.READY && status != FileStatus.TRASH) {
            throw new IllegalStateException("Un processing file can't be deleted");
        }

        if (status != FileStatus.TRASH) { this.status = FileStatus.TRASH; }
        else { this.status = FileStatus.DELETED; }
    }




    // Getter & Setter


    public Long getId() { return id; }

    public User getOwner() { return owner; }

    public String getFileName() { return fileName; }

    public String getContentType() { return contentType; }

    public Long getSizeBytes() { return sizeBytes; }

    public String getStorageKey() { return storageKey; }

    public String getCheckSum() { return checksum; }

    public FileStatus getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public UUID getPublicId() { return publicId; }
}
