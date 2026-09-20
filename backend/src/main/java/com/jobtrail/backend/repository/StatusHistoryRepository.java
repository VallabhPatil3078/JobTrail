package com.jobtrail.backend.repository;

import com.jobtrail.backend.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.jobtrail.backend.model.Application;
import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByApplicationIdOrderByChangedAtDesc(Long applicationId);

    @Query("SELECT COUNT(DISTINCT s.application.id) FROM StatusHistory s WHERE s.toStatus = :status")
    long countDistinctApplicationsByToStatus(@Param("status") Application.StatusEnum status);

    @Query("SELECT COUNT(DISTINCT s.application.id) FROM StatusHistory s WHERE s.toStatus IN :statuses")
    long countDistinctApplicationsByToStatusIn(@Param("statuses") List<Application.StatusEnum> statuses);
}
