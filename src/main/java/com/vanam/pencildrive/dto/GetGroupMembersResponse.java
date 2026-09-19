package com.vanam.pencildrive.dto;
import org.springframework.data.domain.Slice;


public record GetGroupMembersResponse(
        String message,
        Slice<GroupMembersResponse> groupMembers
) {

    public static GetGroupMembersResponse message(String message) {
        return new GetGroupMembersResponse(message, null);
    }

    public static GetGroupMembersResponse from(
            String message,
            Slice<GroupMembersResponse> groupMembers) {
        return new GetGroupMembersResponse(message, groupMembers);
    }
}
