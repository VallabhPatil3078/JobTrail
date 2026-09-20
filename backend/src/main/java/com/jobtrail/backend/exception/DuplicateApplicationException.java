package com.jobtrail.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateApplicationException extends RuntimeException {
    
    private final Long existingId;
    private final String existingCompany;
    private final String existingRole;

    public DuplicateApplicationException(String message, Long existingId, String existingCompany, String existingRole) {
        super(message);
        this.existingId = existingId;
        this.existingCompany = existingCompany;
        this.existingRole = existingRole;
    }
    
    public Long getExistingId() { return existingId; }
    public String getExistingCompany() { return existingCompany; }
    public String getExistingRole() { return existingRole; }
}
