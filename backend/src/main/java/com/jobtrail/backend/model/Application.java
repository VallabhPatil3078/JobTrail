package com.jobtrail.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String role;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @Column(name = "job_url", length = 512)
    private String jobUrl;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "application_status")
    private StatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "data_source", nullable = false)
    private DataSourceEnum source;

    private Double confidence;

    @Column(name = "date_applied")
    private LocalDate dateApplied;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum StatusEnum { APPLIED, OA, INTERVIEW, OFFER, REJECTED, WITHDRAWN }
    public enum DataSourceEnum { MANUAL, EMAIL_DETECTED }
}