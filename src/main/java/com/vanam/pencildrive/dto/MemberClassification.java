package com.vanam.pencildrive.dto;

import com.vanam.pencildrive.domain.GroupMembers;
import com.vanam.pencildrive.domain.User;
import com.vanam.pencildrive.enums.GroupRole;

public interface MemberClassification {

    String getEmail();
    User getUser();         // null => unregistered
    GroupMembers getGroupMember();      // null => not in group
}