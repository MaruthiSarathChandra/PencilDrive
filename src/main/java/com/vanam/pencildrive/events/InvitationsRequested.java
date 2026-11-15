package com.vanam.pencildrive.events;

import java.util.List;
import java.util.Set;

public record InvitationsRequested(
        Long groupId,
        List<String> emails
) {
    public InvitationsRequested {
        emails = List.copyOf(emails);
    }
}
