package com.jobtrail.backend.service;

import com.jobtrail.backend.dto.MatchDto.MatchResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordMatcherServiceTest {

    private final KeywordMatcherService service = new KeywordMatcherService();

    @Test
    void match_EmptyResume_ReturnsZeroMatch() {
        String jobDesc = "We need a strong Java and Spring developer.";
        MatchResult result = service.match("", jobDesc);

        assertThat(result.matchPercentage()).isEqualTo(0.0);
        assertThat(result.matchedKeywords()).isEmpty();
        assertThat(result.missingKeywords()).containsExactlyInAnyOrder("java", "spring");
        assertThat(result.noKeywordsFound()).isFalse();
    }

    @Test
    void match_NoKeywordsInJD_ReturnsNoKeywordsFound() {
        String jobDesc = "We are looking for a strong team player in a fast paced environment.";
        MatchResult result = service.match("I am a team player.", jobDesc);

        assertThat(result.matchPercentage()).isEqualTo(0.0);
        assertThat(result.matchedKeywords()).isEmpty();
        assertThat(result.missingKeywords()).isEmpty();
        assertThat(result.noKeywordsFound()).isTrue();
    }

    @Test
    void match_100PercentMatch() {
        String jobDesc = "Need React, Node, and TypeScript.";
        String resume = "I have 5 years of experience with React, Node, and TypeScript.";
        MatchResult result = service.match(resume, jobDesc);

        assertThat(result.matchPercentage()).isEqualTo(100.0);
        assertThat(result.matchedKeywords()).containsExactlyInAnyOrder("react", "node", "typescript");
        assertThat(result.missingKeywords()).isEmpty();
        assertThat(result.noKeywordsFound()).isFalse();
    }

    @Test
    void match_PartialMatch_CaseInsensitive() {
        String jobDesc = "Looking for PYTHON, Django, and PostgreSQL.";
        String resume = "Experienced in Python and Django, with some MySQL.";
        MatchResult result = service.match(resume, jobDesc);

        // 2 matched (python, django), 1 missing (postgresql) -> 2/3 = 66.7%
        assertThat(result.matchPercentage()).isEqualTo(66.7);
        assertThat(result.matchedKeywords()).containsExactlyInAnyOrder("python", "django");
        assertThat(result.missingKeywords()).containsExactlyInAnyOrder("postgresql");
    }

    @Test
    void match_DictionaryFilter_IgnoresFillerWords() {
        String jobDesc = "Strong team environment requiring Java and React.";
        String resume = "I have a strong background in a team environment using Java.";
        MatchResult result = service.match(resume, jobDesc);

        // JD has 2 valid keywords: java, react. "strong", "team", "environment" are ignored.
        // Resume has 1 valid keyword: java.
        assertThat(result.matchPercentage()).isEqualTo(50.0);
        assertThat(result.matchedKeywords()).containsExactlyInAnyOrder("java");
        assertThat(result.missingKeywords()).containsExactlyInAnyOrder("react");
    }
}
