package com.vanam.pencildrive.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.vanam.pencildrive.domain.FilesMetadata;
import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.Groups;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GroupResponse(
        UUID groupPublicId,
        String groupName) {}
