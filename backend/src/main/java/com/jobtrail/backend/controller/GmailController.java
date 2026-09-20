package com.jobtrail.backend.controller;

import com.jobtrail.backend.model.User;
import com.jobtrail.backend.repository.UserRepository;
import com.jobtrail.backend.service.GmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import com.jobtrail.backend.security.JwtService;

@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
public class GmailController {

    private final GmailService gmailService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/auth-url")
    public ResponseEntity<String> getAuthUrl() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        String state = jwtService.generateStateToken(user.getId());
        String url = gmailService.getAuthorizationUrl(state);
        return ResponseEntity.ok("{\"url\":\"" + url + "\"}");
    }

    @GetMapping("/callback")
    public RedirectView callback(@RequestParam("code") String code, @RequestParam("state") String state) {
        Long userId = jwtService.extractUserIdFromStateToken(state);
        gmailService.exchangeCode(code, userId);
        return new RedirectView(frontendUrl + "?gmail_connected=true");
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok("{\"status\":\"" + user.getGmailConnectionStatus() + "\"}");
    }



    @PostMapping("/sync")
    public ResponseEntity<Void> syncNow() {
        boolean started = gmailService.triggerManualSync();
        if (started) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
