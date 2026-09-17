package com.jobtrail.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "raw_emails")
public class RawEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true)
    private String messageId;

    @Column(columnDefinition = "TEXT")
    private String subject;

    @Column(name = "sender")
    private String sender;

    @Column(columnDefinition = "TEXT")
    private String snippet;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(nullable = false)
    private boolean processed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
