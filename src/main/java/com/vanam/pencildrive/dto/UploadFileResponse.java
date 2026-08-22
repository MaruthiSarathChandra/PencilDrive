package com.vanam.pencildrive.dto;

import com.vanam.pencildrive.dto.FileResponse;

public record UploadFileResponse(
        String message,
        FileResponse file) { }
