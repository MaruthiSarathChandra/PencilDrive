package com.vanam.pencildrive.CustomException;

public class FileUploadException extends RuntimeException{
    public FileUploadException (String message, Throwable cause) {
        super(message, cause);
    }
}
