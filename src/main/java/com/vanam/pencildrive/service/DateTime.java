package com.vanam.pencildrive.service;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTime {


    //1. Method to Generate CreatedAt
    private LocalDateTime dateTime() {

        LocalDateTime dT = java.time.LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return dT;
    }


    //2. Method to Generate ExpiringAt
    private LocalDateTime expiringDateTIme(LocalDateTime dateTime) {

        return dateTime.plusDays(13);

    }


    //1. Getter For CreatedAt
    public LocalDateTime getDateTime() {
        return dateTime();
    }


    // 2. Getter For ExpiringAt
    public LocalDateTime getExpiringDateTime(LocalDateTime dateTime) {
        return expiringDateTIme(dateTime);
    }


}
