package com.vanam.pencildrive.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class PasswordEncoder {

    // Create an instance of the encoder
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String plainPassword) {
        // BCrypt handles salting automatically
        return encoder.encode(plainPassword);
    }

    public static Boolean matches(String plainPassword, String hashedPassword) {
        // .matches() correctly handles checking the salt and comparing the hash
        return encoder.matches(plainPassword, hashedPassword);
    }

}
