package com.jobtrail.backend.dto;

import java.util.List;

public class MatchDto {
    public record MatchResult(
        double matchPercentage,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        boolean noKeywordsFound
    ) {}
}
