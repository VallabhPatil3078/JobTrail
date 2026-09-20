package com.jobtrail.backend.controller;

import com.jobtrail.backend.model.Application;
import com.jobtrail.backend.model.RawEmail;
import com.jobtrail.backend.model.SuggestedApplication;
import com.jobtrail.backend.repository.ApplicationRepository;
import com.jobtrail.backend.repository.RawEmailRepository;
import com.jobtrail.backend.repository.SuggestedApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/demo")
@Profile("demo")
@RequiredArgsConstructor
public class DemoDataController {

    private final ApplicationRepository applicationRepository;
    private final RawEmailRepository rawEmailRepository;
    private final SuggestedApplicationRepository suggestedApplicationRepository;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/seed")
    @Transactional
    public ResponseEntity<String> seedData() {
        if (applicationRepository.count() > 0 || suggestedApplicationRepository.count() > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Data already exists");
        }

        // Create RawEmails
        RawEmail e1 = createRawEmail("Stripe", "hello@stripe.com", "Thanks for applying to the Software Engineer role.");
        RawEmail e2 = createRawEmail("Vercel", "careers@vercel.com", "Next steps for Frontend Engineer.");
        RawEmail e3 = createRawEmail("Cloudflare", "jobs@cloudflare.com", "Application Received: Systems Engineer");

        // Seed pending suggestions
        SuggestedApplication s1 = new SuggestedApplication();
        s1.setRawEmail(e3);
        s1.setExtractedCompany("Cloudflare");
        s1.setExtractedRole("Systems Engineer");
        s1.setExtractedDate(LocalDate.now().minusDays(1));
        s1.setConfidenceScore(92.0);
        s1.setStatus(SuggestedApplication.SuggestionStatusEnum.PENDING);
        suggestedApplicationRepository.save(s1);

        // Seed some confirmed applications
        Application a1 = new Application();
        a1.setCompany("Stripe");
        a1.setRole("Software Engineer");
        a1.setStatus(Application.StatusEnum.APPLIED);
        a1.setSource(Application.DataSourceEnum.EMAIL_DETECTED);
        a1.setSourceRawEmail(e1);
        a1.setDateApplied(LocalDate.now().minusDays(10));
        applicationRepository.save(a1);
        insertHistory(a1.getId(), Application.StatusEnum.APPLIED, Application.StatusEnum.APPLIED, "Initial application", LocalDateTime.now().minusDays(10));
        
        Application a2 = new Application();
        a2.setCompany("Vercel");
        a2.setRole("Frontend Engineer");
        a2.setStatus(Application.StatusEnum.INTERVIEW);
        a2.setSource(Application.DataSourceEnum.EMAIL_DETECTED);
        a2.setSourceRawEmail(e2);
        a2.setDateApplied(LocalDate.now().minusDays(14));
        applicationRepository.save(a2);
        insertHistory(a2.getId(), Application.StatusEnum.APPLIED, Application.StatusEnum.APPLIED, "Applied via email", LocalDateTime.now().minusDays(14));
        insertHistory(a2.getId(), Application.StatusEnum.APPLIED, Application.StatusEnum.OA, "OA received", LocalDateTime.now().minusDays(10));
        insertHistory(a2.getId(), Application.StatusEnum.OA, Application.StatusEnum.INTERVIEW, "Interview scheduled", LocalDateTime.now().minusDays(3));

        Application a3 = new Application();
        a3.setCompany("Netflix");
        a3.setRole("Senior Engineer");
        a3.setStatus(Application.StatusEnum.REJECTED);
        a3.setSource(Application.DataSourceEnum.MANUAL);
        a3.setDateApplied(LocalDate.now().minusDays(30));
        applicationRepository.save(a3);
        insertHistory(a3.getId(), Application.StatusEnum.APPLIED, Application.StatusEnum.APPLIED, "Manual entry", LocalDateTime.now().minusDays(30));
        insertHistory(a3.getId(), Application.StatusEnum.APPLIED, Application.StatusEnum.REJECTED, "Auto-reject", LocalDateTime.now().minusDays(29));

        return ResponseEntity.ok("Demo data seeded successfully");
    }

    private RawEmail createRawEmail(String company, String sender, String snippet) {
        RawEmail re = new RawEmail();
        re.setMessageId("demo-" + UUID.randomUUID().toString());
        re.setSubject("Application update from " + company);
        re.setSender(sender);
        re.setSnippet(snippet);
        re.setReceivedAt(LocalDateTime.now().minusDays((long)(Math.random()*10)));
        return rawEmailRepository.save(re);
    }

    private void insertHistory(Long appId, Application.StatusEnum from, Application.StatusEnum to, String note, LocalDateTime time) {
        jdbcTemplate.update("INSERT INTO status_history (application_id, from_status, to_status, note, data_source, changed_at) VALUES (?, ?::application_status, ?::application_status, ?, 'MANUAL'::data_source, ?)",
                appId, from.name(), to.name(), note, time);
    }
}
