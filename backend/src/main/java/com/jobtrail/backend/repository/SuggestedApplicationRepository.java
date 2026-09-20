package com.jobtrail.backend.repository;

import com.jobtrail.backend.model.SuggestedApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SuggestedApplicationRepository extends JpaRepository<SuggestedApplication, Long> {
    @Query("SELECT s FROM SuggestedApplication s JOIN FETCH s.rawEmail WHERE s.status = :status")
    List<SuggestedApplication> findByStatusWithRawEmail(@Param("status") SuggestedApplication.SuggestionStatusEnum status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SuggestedApplication s WHERE s.id = :id")
    Optional<SuggestedApplication> findByIdForUpdate(@Param("id") Long id);
}
