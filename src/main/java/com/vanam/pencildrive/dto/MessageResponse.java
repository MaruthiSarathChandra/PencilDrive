package com.vanam.pencildrive.dto;



public record MessageResponse(String message) {


    public static MessageResponse from(String message) {
        return new MessageResponse(message);
    }


}
