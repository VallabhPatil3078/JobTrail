package com.jobtrail.backend.service;

import com.jobtrail.backend.dto.ReminderDto;
import com.jobtrail.backend.model.Application;
import com.jobtrail.backend.model.Reminder;
import com.jobtrail.backend.repository.ApplicationRepository;
import com.jobtrail.backend.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final ApplicationRepository applicationRepository;

    public void createOrUpdateReminder(Long applicationId, Reminder.ReminderType type, LocalDateTime dueDate) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        Optional<Reminder> existing = reminderRepository.findByApplicationIdAndTypeAndCompletedAtIsNull(applicationId, type);
        if (existing.isPresent()) {
            Reminder reminder = existing.get();
            reminder.setDueDate(dueDate);
            reminderRepository.save(reminder);
        } else {
            Reminder reminder = new Reminder();
            reminder.setApplication(app);
            reminder.setType(type);
            reminder.setDueDate(dueDate);
            reminderRepository.save(reminder);
        }
    }

    public List<ReminderDto> getActiveReminders() {
        return reminderRepository.findAllActive().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void completeReminder(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        
        reminder.setCompletedAt(LocalDateTime.now());
        reminderRepository.save(reminder);
    }
    
    public void deleteReminder(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        
        reminderRepository.delete(reminder);
    }

    private ReminderDto toDto(Reminder reminder) {
        return new ReminderDto(
                reminder.getId(),
                reminder.getApplication().getId(),
                reminder.getApplication().getCompany(),
                reminder.getApplication().getRole(),
                reminder.getType().name(),
                reminder.getDueDate(),
                reminder.getCreatedAt()
        );
    }
}
