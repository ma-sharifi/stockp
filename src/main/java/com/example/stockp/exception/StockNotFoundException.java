package com.example.stockp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author Mahdi Sharifi
 * When entity not found throw this exception
 */
@Getter
public class StockNotFoundException extends AbstractThrowable {

    public StockNotFoundException(String title) {
        super("Could not find the stock: " + title, HttpStatus.NOT_FOUND, 4040);
    }

}
