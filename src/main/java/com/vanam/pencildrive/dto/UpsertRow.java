package com.vanam.pencildrive.dto;

import com.vanam.pencildrive.enums.GroupRole;

public record UpsertRow(
        Long userId,
        GroupRole role){
}
