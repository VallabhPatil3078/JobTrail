package com.jobtrail.backend.controller;

import com.jobtrail.backend.model.Application.StatusEnum;
import com.jobtrail.backend.model.Application.DataSourceEnum;
import com.jobtrail.backend.repository.ApplicationRepository;
import com.jobtrail.backend.repository.StatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    @GetMapping
    public Map<String, Object> getStats() {
        long total = applicationRepository.count();
        long active = applicationRepository.countByStatusNotIn(List.of(StatusEnum.REJECTED, StatusEnum.WITHDRAWN));
        long emailDetected = applicationRepository.countBySource(DataSourceEnum.EMAIL_DETECTED);
        
        double emailDetectedPct = total == 0 ? 0 : Math.round(((double) emailDetected / total) * 100.0);
        
        long responded = statusHistoryRepository.countDistinctApplicationsByToStatusIn(List.of(StatusEnum.OA, StatusEnum.INTERVIEW, StatusEnum.OFFER));
        double responseRate = total == 0 ? 0 : Math.round(((double) responded / total) * 100.0);

        long oa = statusHistoryRepository.countDistinctApplicationsByToStatus(StatusEnum.OA);
        long interview = statusHistoryRepository.countDistinctApplicationsByToStatus(StatusEnum.INTERVIEW);
        long offer = statusHistoryRepository.countDistinctApplicationsByToStatus(StatusEnum.OFFER);

        return Map.of(
            "total", total,
            "active", active,
            "responseRate", responseRate,
            "emailDetectedPct", emailDetectedPct,
            "stages", Map.of(
                "OA", oa,
                "INTERVIEW", interview,
                "OFFER", offer
            )
        );
    }
}
