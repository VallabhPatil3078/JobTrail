package com.jobtrail.backend.service;

import com.jobtrail.backend.model.RawEmail;
import com.jobtrail.backend.model.SuggestedApplication;
import com.jobtrail.backend.repository.RawEmailRepository;
import com.jobtrail.backend.repository.SuggestedApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailParsingService {

    private final RawEmailRepository rawEmailRepository;
    private final SuggestedApplicationRepository suggestedApplicationRepository;

    // Common patterns in application confirmation emails
    private static final Pattern SUBJECT_PATTERN_1 = Pattern.compile("application for (?:the )?(.*) (?:position|role) at (.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUBJECT_PATTERN_2 = Pattern.compile("applying to (.*) at (.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUBJECT_PATTERN_3 = Pattern.compile("your application (?:to|with) (.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATS_SENDER_PATTERN = Pattern.compile("@(greenhouse\\.io|lever\\.co|myworkday\\.com|ashbyhq\\.com|workable\\.com)");

    @Transactional
    public void processUnprocessedEmails() {
        List<RawEmail> unprocessedEmails = rawEmailRepository.findByProcessedFalse();
        if (unprocessedEmails.isEmpty()) {
            return;
        }

        log.info("Found {} unprocessed raw emails. Starting parsing...", unprocessedEmails.size());

        for (RawEmail email : unprocessedEmails) {
            try {
                parseAndSuggest(email);
                email.setProcessed(true);
                rawEmailRepository.save(email);
            } catch (Exception e) {
                log.error("Failed to parse email with ID: {}", email.getId(), e);
            }
        }
    }

    private void parseAndSuggest(RawEmail email) {
        String subject = email.getSubject() != null ? email.getSubject() : "";
        String sender = email.getSender() != null ? email.getSender() : "";
        
        String extractedCompany = null;
        String extractedRole = null;
        double confidence = 0.0;

        // 1. Check ATS Sender
        Matcher atsMatcher = ATS_SENDER_PATTERN.matcher(sender);
        if (atsMatcher.find()) {
            confidence += 40.0; // High signal that this is a job application email
        }

        // 2. Try Regex Patterns on Subject
        Matcher m1 = SUBJECT_PATTERN_1.matcher(subject);
        if (m1.find()) {
            extractedRole = m1.group(1).trim();
            extractedCompany = m1.group(2).trim();
            confidence += 50.0;
        } else {
            Matcher m2 = SUBJECT_PATTERN_2.matcher(subject);
            if (m2.find()) {
                extractedRole = m2.group(1).trim();
                extractedCompany = m2.group(2).trim();
                confidence += 50.0;
            } else {
                Matcher m3 = SUBJECT_PATTERN_3.matcher(subject);
                if (m3.find()) {
                    extractedCompany = m3.group(1).trim();
                    confidence += 30.0;
                }
            }
        }

        // 3. Fallback heuristics if regex didn't catch the company perfectly
        if (extractedCompany == null && subject.toLowerCase().contains("application")) {
            // Very naive fallback: Try to extract sender name (e.g. "Google <no-reply@google.com>" -> "Google")
            int bracketIndex = sender.indexOf("<");
            if (bracketIndex > 0) {
                extractedCompany = sender.substring(0, bracketIndex).trim();
                confidence += 20.0;
            }
        }

        // 4. Cap confidence
        confidence = Math.min(confidence, 99.0);

        // 5. Create suggestion if we have at least a company name
        if (extractedCompany != null && !extractedCompany.isEmpty()) {
            SuggestedApplication suggestion = new SuggestedApplication();
            suggestion.setRawEmail(email);
            suggestion.setExtractedCompany(extractedCompany);
            suggestion.setExtractedRole(extractedRole != null ? extractedRole : "Unknown Role");
            suggestion.setExtractedDate(email.getReceivedAt() != null ? email.getReceivedAt().toLocalDate() : null);
            suggestion.setConfidenceScore(confidence);
            suggestion.setStatus(SuggestedApplication.SuggestionStatusEnum.PENDING);
            
            suggestedApplicationRepository.save(suggestion);
            log.info("Created suggestion for {} - Role: {} (Confidence: {}%)", extractedCompany, extractedRole, confidence);
        } else {
            log.info("Could not extract company from email {}. Marking as processed but no suggestion generated.", email.getId());
        }
    }
}
