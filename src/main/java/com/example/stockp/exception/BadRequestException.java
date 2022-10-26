package com.example.stockp.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Mahdi Sharifi
 */

public class BadRequestException extends AbstractThrowable{

    public BadRequestException(String reason) {
        super("Bad request! Something wrong in client request! Reason: "+reason, HttpStatus.BAD_REQUEST, 4001);
    }
}
