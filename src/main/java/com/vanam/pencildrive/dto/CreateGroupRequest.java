package com.vanam.pencildrive.dto;

import com.vanam.pencildrive.domain.FilesMetadata;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateGroupRequest(
        @NotBlank(message = "Group name cannot be empty")
        @Size(max = 50, message = "Group name must be under 50 characters")
        String groupName,

        @NotNull(message = "Members list can't be null")
        @Size(max = 4, message = "You can reached max members at a time")
        List<@Valid GroupMembersRequest> members,

        FilesMetadata file
) {

}
