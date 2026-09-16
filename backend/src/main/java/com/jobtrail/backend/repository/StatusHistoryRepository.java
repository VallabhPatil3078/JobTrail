package com.jobtrail.backend.repository;

import com.jobtrail.backend.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByApplicationIdOrderByChangedAtDesc(Long applicationId);
}
