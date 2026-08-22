package com.vanam.pencildrive.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;







@Controller
@RequestMapping("/api")
public class DriveController {


    @GetMapping("/drive")
    public String showDrive() {
        return "home";
    }

}
