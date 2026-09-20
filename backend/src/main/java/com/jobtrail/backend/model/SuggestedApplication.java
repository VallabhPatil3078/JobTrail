package com.jobtrail.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "suggested_applications")
public class SuggestedApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_email_id", nullable = false)
    private RawEmail rawEmail;

    @Column(name = "extracted_company")
    private String extractedCompany;

    @Column(name = "extracted_role")
    private String extractedRole;

    @Column(name = "extracted_date")
    private LocalDate extractedDate;

    @Column(name = "confidence_score", columnDefinition = "NUMERIC(5,2)")
    private Double confidenceScore;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "suggestion_status", nullable = false)
    private SuggestionStatusEnum status = SuggestionStatusEnum.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum SuggestionStatusEnum { PENDING, CONFIRMED, REJECTED }
}
