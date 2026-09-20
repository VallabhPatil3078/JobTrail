package com.jobtrail.backend.repository;

import com.jobtrail.backend.model.SuggestedApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestedApplicationRepository extends JpaRepository<SuggestedApplication, Long> {
    List<SuggestedApplication> findByStatus(SuggestedApplication.SuggestionStatusEnum status);
}
