package com.vanam.pencildrive.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetGroups(
        UUID groupPublicId,
        String groupName,
        List<GroupMembersResponse> groupMembersResponse
) {
}
