package com.atosinterview.hrmanagerbe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public class CreatingRoleException extends RuntimeException{

    public CreatingRoleException(String message) {
        super(message);
    }

    public CreatingRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
