package com.vanam.pencildrive.CustomException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GroupMemberAlreadyExistsException extends RuntimeException {
    public GroupMemberAlreadyExistsException(String message) {
        super(message);
    }
}
