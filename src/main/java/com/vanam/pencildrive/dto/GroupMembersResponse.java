package com.vanam.pencildrive.dto;

import com.vanam.pencildrive.enums.GroupRole;

public record GroupMembersResponse(
        String email,
        GroupRole groupRole
) {
}
