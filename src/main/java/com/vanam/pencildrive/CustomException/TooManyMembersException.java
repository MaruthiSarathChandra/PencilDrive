package com.vanam.pencildrive.CustomException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class TooManyMembersException extends RuntimeException {
    public TooManyMembersException(String MAX_REQUEST) {
        super(MAX_REQUEST);
    }
}
