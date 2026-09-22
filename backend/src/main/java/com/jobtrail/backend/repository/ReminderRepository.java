package com.jobtrail.backend.repository;

import com.jobtrail.backend.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    
    @Query("SELECT r FROM Reminder r JOIN FETCH r.application a WHERE r.completedAt IS NULL ORDER BY r.dueDate ASC")
    List<Reminder> findAllActive();

    Optional<Reminder> findByApplicationIdAndTypeAndCompletedAtIsNull(Long applicationId, Reminder.ReminderType type);

    long countByApplicationIdAndCompletedAtIsNull(Long applicationId);
}
