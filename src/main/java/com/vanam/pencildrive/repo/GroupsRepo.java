package com.vanam.pencildrive.repo;
import com.vanam.pencildrive.domain.Groups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.crypto.spec.OAEPParameterSpec;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface GroupsRepo extends JpaRepository<Groups, Long> {

    boolean existsByOwner_IdAndGroupName(Long ownerId, String groupName);

    Optional<Groups> findByPublicGroupId(UUID publicGroupId);

}
