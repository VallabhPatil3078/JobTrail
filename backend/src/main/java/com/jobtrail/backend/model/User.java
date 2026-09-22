package com.jobtrail.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "encrypted_refresh_token", length = 512)
    private String encryptedRefreshToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "gmail_connection_status", nullable = false)
    private GmailConnectionStatus gmailConnectionStatus = GmailConnectionStatus.DISCONNECTED;

    public enum GmailConnectionStatus {
        CONNECTED, DISCONNECTED, NEEDS_RECONNECT
    }

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}