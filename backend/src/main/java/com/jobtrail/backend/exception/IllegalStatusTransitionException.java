package com.jobtrail.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalStatusTransitionException extends RuntimeException {
    public IllegalStatusTransitionException(String message) {
        super(message);
    }
}
