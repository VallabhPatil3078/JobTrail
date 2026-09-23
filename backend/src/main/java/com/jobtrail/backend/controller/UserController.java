package com.jobtrail.backend.controller;

import com.jobtrail.backend.model.User;
import com.jobtrail.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    public record UserProfileResponse(
            String email,
            User.GmailConnectionStatus gmailConnectionStatus,
            boolean hasResumeText
    ) {}

    public record ResumeUpdateRequest(
            String resumeText
    ) {}

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        boolean hasResumeText = user.getResumeText() != null && !user.getResumeText().isBlank();
        
        return ResponseEntity.ok(new UserProfileResponse(
                user.getEmail(),
                user.getGmailConnectionStatus(),
                hasResumeText
        ));
    }

    @PutMapping("/me/resume")
    public ResponseEntity<Void> updateResume(@RequestBody ResumeUpdateRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        user.setResumeText(request.resumeText());
        userRepository.save(user);
        
        return ResponseEntity.ok().build();
    }
}
