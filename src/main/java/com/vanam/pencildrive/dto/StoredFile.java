package com.vanam.pencildrive.dto;

public record StoredFile(
        String storageKey,
        String checkSum,
        long fileSize
) { }
