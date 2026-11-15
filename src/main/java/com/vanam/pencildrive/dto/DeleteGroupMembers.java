package com.vanam.pencildrive.dto;

import java.util.List;
import java.util.UUID;

public record DeleteGroupMembers(
    UUID publicGroupId,
    List<GroupMembersRequest> groupMembersRequest
){}
