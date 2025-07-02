package com.yogant.journal.controller;


import com.yogant.journal.service.GoogleOAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {

    private final GoogleOAuthService googleOAuthService;

    public GoogleAuthController(GoogleOAuthService googleOAuthService) {
        this.googleOAuthService = googleOAuthService;
    }

    @GetMapping("/callback")
    public ResponseEntity<com.yogant.journal.dto.GoogleAuthResponseDTO> handleGoogleCallback(@RequestParam String code) {
        com.yogant.journal.dto.GoogleAuthResponseDTO response = googleOAuthService.authHandler(code);
        return ResponseEntity.ok(response);
    }
}
