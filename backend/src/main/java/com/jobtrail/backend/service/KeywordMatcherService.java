package com.jobtrail.backend.service;

import com.jobtrail.backend.dto.MatchDto.MatchResult;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KeywordMatcherService {

    // A curated dictionary of technical keywords to filter out noise
    private static final Set<String> TECH_DICTIONARY = Set.of(
            "java", "python", "javascript", "typescript", "c++", "c#", "ruby", "go", "rust", "php",
            "swift", "kotlin", "scala", "react", "angular", "vue", "node", "express", "spring", "springboot",
            "django", "flask", "laravel", "ruby on rails", "html", "css", "sass", "less", "tailwind",
            "bootstrap", "materialui", "sql", "mysql", "postgresql", "postgres", "mongodb", "mongo",
            "redis", "elasticsearch", "cassandra", "oracle", "docker", "kubernetes", "k8s", "aws", "azure",
            "gcp", "google cloud", "terraform", "ansible", "jenkins", "github actions", "gitlab ci", "circleci",
            "travisci", "linux", "unix", "bash", "shell", "powershell", "git", "github", "gitlab", "bitbucket",
            "jira", "confluence", "trello", "agile", "scrum", "kanban", "rest", "graphql", "grpc", "soap",
            "oauth", "jwt", "saml", "openid", "microservices", "serverless", "lambda", "s3", "ec2", "rds",
            "dynamodb", "sqs", "sns", "kafka", "rabbitmq", "activemq", "memcached", "nginx",
            "apache", "tomcat", "iis", "machine learning", "ai", "artificial intelligence", "data science",
            "data engineering", "big data", "hadoop", "spark", "flink", "hive", "pig", "tableau", "powerbi",
            "looker", "snowflake", "redshift", "bigquery", "airflow", "luigi", "dbt", "pytorch", "tensorflow",
            "keras", "scikit-learn", "pandas", "numpy", "matplotlib", "seaborn", "jupyter", "r", "matlab",
            "ios", "android", "react native", "flutter", "xamarin", "ionic", "cordova", "electron",
            "macos", "windows", "ubuntu", "debian", "centos", "redhat", "alpine", "vmware", "virtualbox",
            "vagrant", "tcp", "udp", "http", "https", "dns", "dhcp", "ftp", "ssh", "ssl", "tls", "ip",
            "ipv4", "ipv6", "bgp", "ospf", "vlan", "vpn", "ipsec", "openvpn", "wireguard", "cisco", "juniper",
            "fortinet", "palo alto", "checkpoint", "f5", "load balancer", "firewall", "ids", "ips", "siem",
            "splunk", "elk", "datadog", "new relic", "appdynamics", "dynatrace", "prometheus", "grafana",
            "zabbix", "nagios", "icinga", "sensu", "pagerduty", "opsgenie", "victorops", "slack", "teams",
            "zoom", "webex", "gsuite", "office365", "salesforce", "hubspot", "marketo", "pardot", "zendesk",
            "servicenow", "workday", "sap", "oracle ebs", "peoplesoft", "netsuite", "dynamics", "epic",
            "cerner", "meditech", "allscripts", "athenahealth", "eclinicalworks", "nextgen", "greenway"
    );

    public MatchResult match(String resumeText, String jobDescription) {
        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            return new MatchResult(0.0, List.of(), List.of(), true);
        }

        Set<String> jdKeywords = extractKeywords(jobDescription);
        if (jdKeywords.isEmpty()) {
            return new MatchResult(0.0, List.of(), List.of(), true);
        }

        Set<String> resumeKeywords = resumeText == null || resumeText.trim().isEmpty() ? 
                new HashSet<>() : extractKeywords(resumeText);

        List<String> matched = jdKeywords.stream()
                .filter(resumeKeywords::contains)
                .sorted()
                .collect(Collectors.toList());

        List<String> missing = jdKeywords.stream()
                .filter(kw -> !resumeKeywords.contains(kw))
                .sorted()
                .collect(Collectors.toList());

        double percentage = ((double) matched.size() / jdKeywords.size()) * 100.0;
        // Round to 1 decimal place
        percentage = Math.round(percentage * 10.0) / 10.0;

        return new MatchResult(percentage, matched, missing, false);
    }

    private Set<String> extractKeywords(String text) {
        if (text == null) return new HashSet<>();

        String[] tokens = text.toLowerCase().split("[^a-z0-9+#]+");
        
        return Arrays.stream(tokens)
                .filter(token -> !token.isEmpty())
                .filter(TECH_DICTIONARY::contains)
                .collect(Collectors.toSet());
    }
}
