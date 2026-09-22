package com.jobtrail.backend.controller;

import com.jobtrail.backend.dto.ReminderDto;
import com.jobtrail.backend.model.Reminder.ReminderType;
import com.jobtrail.backend.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping("/api/reminders")
    public ResponseEntity<List<ReminderDto>> getActiveReminders() {
        return ResponseEntity.ok(reminderService.getActiveReminders());
    }

    @PatchMapping("/api/reminders/{id}/complete")
    public ResponseEntity<Void> completeReminder(@PathVariable Long id) {
        reminderService.completeReminder(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/reminders/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/applications/{appId}/reminders")
    public ResponseEntity<Void> createReminder(
            @PathVariable Long appId,
            @RequestParam String type,
            @RequestParam String dueDate) {
        reminderService.createOrUpdateReminder(appId, ReminderType.valueOf(type), LocalDateTime.parse(dueDate));
        return ResponseEntity.ok().build();
    }
}
