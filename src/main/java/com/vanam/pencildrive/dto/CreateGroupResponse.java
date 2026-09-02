package com.vanam.pencildrive.dto;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateGroupResponse(

        String message,
        GroupResponse groups,
        List<GroupMembersResponse> groupMembersResponse,
        UUID file
) {

    public static CreateGroupResponse from(
            String message,
            GroupResponse groups,
            List<GroupMembersResponse> groupMembersResponse,
            UUID file
    ) {
        return new CreateGroupResponse(
                message,
                groups,
                groupMembersResponse,
                file
        );
    }

    public static CreateGroupResponse message(String message) {
        return new CreateGroupResponse(
                message,
                null,
                null,
                null);
    }

    public static CreateGroupResponse createGroupWithGroupMembers(
            String message,
            GroupResponse groups,
            List<GroupMembersResponse> groupMembersResponses
    ) {
        return new CreateGroupResponse(
                message,
                groups,
                groupMembersResponses,
                null);
    }
}
