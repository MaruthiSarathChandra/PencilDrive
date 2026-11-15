package com.vanam.pencildrive.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record AddMemberToExistingGroup(

    @NotBlank(message = "groupId cannot be empty")
    @Valid UUID publicGroupId,

    @NotNull(message = "Members list can't be null")
    @Size(max = 4, message = "You can reached max members at a time")
    List<@Valid GroupMembersRequest> members
) {}
