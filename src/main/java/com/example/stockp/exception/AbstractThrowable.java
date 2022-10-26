package com.example.stockp.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author Mahdi Sharifi
 */

@AllArgsConstructor @Getter
public class AbstractThrowable extends RuntimeException {

    private final String message;//"Could not find account with id: "
    private final HttpStatus httpStatus; // NOT_FOUND
    private final int errorCode;
}
