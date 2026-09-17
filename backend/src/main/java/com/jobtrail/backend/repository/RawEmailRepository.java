package com.jobtrail.backend.repository;

import com.jobtrail.backend.model.RawEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RawEmailRepository extends JpaRepository<RawEmail, Long> {
    Optional<RawEmail> findByMessageId(String messageId);
    List<RawEmail> findByProcessedFalse();
}
