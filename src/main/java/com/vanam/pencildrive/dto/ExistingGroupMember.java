package com.vanam.pencildrive.dto;
import com.vanam.pencildrive.enums.GroupRole;



//un-used
public record ExistingGroupMember(
        String email,
        GroupRole role
) {}