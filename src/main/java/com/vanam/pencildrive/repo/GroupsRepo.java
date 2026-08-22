package com.vanam.pencildrive.repo;
import com.vanam.pencildrive.domain.Groups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.*;


@Repository
public interface GroupsRepo extends JpaRepository<Groups, Long> {



}
