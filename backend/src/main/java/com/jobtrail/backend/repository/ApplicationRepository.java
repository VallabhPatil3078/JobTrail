package com.jobtrail.backend.repository;

import com.jobtrail.backend.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCompanyIgnoreCase(String company);
}