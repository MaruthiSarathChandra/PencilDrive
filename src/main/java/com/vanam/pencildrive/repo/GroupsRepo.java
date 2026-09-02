package com.vanam.pencildrive.repo;
import com.vanam.pencildrive.domain.Groups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GroupsRepo extends JpaRepository<Groups, Long> {

    boolean existsByOwner_IdAndGroupName(Long ownerId, String groupName);

}
