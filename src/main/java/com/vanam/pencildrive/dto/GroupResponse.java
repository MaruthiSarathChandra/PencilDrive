package com.vanam.pencildrive.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.Groups;
import jakarta.annotation.Nullable;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GroupResponse(
        String message,
        Groups group,
        List<GroupMembers> groupMembers,
        FilesMetadata filesMetadata
    ){

    public static GroupResponse from(String message,
                                     Groups group,
                                     List<GroupMembers> groupMembers,
                                     FilesMetadata filesMetadata) {
        return new GroupResponse(
                message,
                group,
                groupMembers,
                filesMetadata);
    }


    public static GroupResponse message (String message) {
        return new GroupResponse(message,
                null,
                null,
                null);
    }
    public static GroupResponse createGroupWithGroupMembers(String message,
                                     Groups group,
                                     List<GroupMembers> groupMembers
                                     ) {
        return new GroupResponse(message, group, groupMembers, null);
    }



}
