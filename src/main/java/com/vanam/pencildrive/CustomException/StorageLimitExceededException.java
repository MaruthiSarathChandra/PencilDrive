package com.vanam.pencildrive.CustomException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
public class StorageLimitExceededException extends RuntimeException{

    public StorageLimitExceededException(String message) {
        super(message);
    }
}
