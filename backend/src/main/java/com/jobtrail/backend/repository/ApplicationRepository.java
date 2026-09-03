package com.jobtrail.backend.repository;

import com.jobtrail.backend.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}