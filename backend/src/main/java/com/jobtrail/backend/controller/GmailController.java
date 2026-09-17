package com.jobtrail.backend.controller;

import com.jobtrail.backend.model.User;
import com.jobtrail.backend.repository.UserRepository;
import com.jobtrail.backend.service.GmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
public class GmailController {

    private final GmailService gmailService;
    private final UserRepository userRepository;

    @GetMapping("/connect")
    public RedirectView connect() {
        return new RedirectView(gmailService.getAuthorizationUrl());
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam("code") String code) {
        // In a real app with a frontend, we might receive state to verify user,
        // or the frontend handles the redirect. Since it's a backend redirect, 
        // we'll fetch the first user (for our single-tenant local app).
        User user = userRepository.findAll().stream().findFirst().orElseThrow(() -> new RuntimeException("No user found in DB! Please register a user first."));
        gmailService.exchangeCode(code, user.getId());
        
        return ResponseEntity.ok("Gmail successfully connected! You can now close this tab.");
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncNow() {
        gmailService.syncEmails();
        return ResponseEntity.ok().build();
    }
}
