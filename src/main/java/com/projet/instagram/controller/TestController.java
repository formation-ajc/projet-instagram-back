package com.projet.instagram.controller;

import com.projet.instagram.schema.response.security.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("${api.baseURL}/test")
public class TestController {

    @GetMapping("/user")
//    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<MessageResponse> userAccess() {
        return ResponseEntity.ok(new MessageResponse("User Here."));
    }


    @GetMapping("/admin")
//    @PreAuthorize("hasAnyAuthority('ADMIN')")
//    @Secured("ADMIN")
    public String adminAccess() {
        return "Admin Here.";
    }

    @GetMapping("/super")
//    @PreAuthorize("hasAnyAuthority('SUPER')")
//    @Secured("ADMIN")
    public String superAccess() {
        return "Super Here.";
    }
}
