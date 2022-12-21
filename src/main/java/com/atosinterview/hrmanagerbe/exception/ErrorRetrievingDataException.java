package com.atosinterview.hrmanagerbe.exception;

public class ErrorRetrievingDataException  extends RuntimeException{

    public ErrorRetrievingDataException(String message) {
        super(message);
    }

    public ErrorRetrievingDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
