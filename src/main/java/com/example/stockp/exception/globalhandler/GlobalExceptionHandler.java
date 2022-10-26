package com.example.stockp.exception.globalhandler;


import com.example.stockp.exception.AbstractThrowable;
import com.example.stockp.exception.BadRequestException;
import com.example.stockp.exception.StockNotFoundException;
import com.example.stockp.service.dto.ResponseDto;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mahdi Sharifi
 * @since 10/6/22
 */

@ControllerAdvice
@NoArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseDto<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        ResponseDto<Void> responseDto = ResponseDto.<Void>builder().httpStatus(HttpStatus.BAD_REQUEST).errorCode(4006)
                .message("#Validation method argument type mismatch error!").details(ex.getMessage() + " #parameter: " + ex.getParameter()).build();
        return new ResponseEntity<>(responseDto, responseDto.getHttpStatus());
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<ResponseDto<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ResponseDto<Void> responseDto = ResponseDto.<Void>builder().httpStatus(HttpStatus.BAD_REQUEST)
                .message("#Validation method argument error!").errorCode(4009).build();
        if (log.isDebugEnabled()) {
            responseDto.setDetails(ex.getBindingResult().toString());
            responseDto.setErrors(errors);
        }
        return new ResponseEntity<>(responseDto, responseDto.getHttpStatus());
    }

    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<ResponseDto<Void>> handleHttpRequestMethodNotSupportedException
            (HttpRequestMethodNotSupportedException ex, WebRequest request) {

        ResponseDto<Void> responseDto = ResponseDto.<Void>builder().httpStatus(HttpStatus.METHOD_NOT_ALLOWED)
                .message("#Method Not Allowed!").errorCode(4050).build();
        if (log.isDebugEnabled())
            responseDto.setDetails(ex.getLocalizedMessage() + " ;" + request);
        return new ResponseEntity<>(responseDto, responseDto.getHttpStatus());
    }


    @ExceptionHandler(value = {EmptyResultDataAccessException.class})
    public ResponseEntity<ResponseDto<Void>> handleEmptyResultDataAccessException(EmptyResultDataAccessException ex) {

        ResponseDto<Void> responseDto = ResponseDto.<Void>builder().httpStatus(HttpStatus.NOT_FOUND)
                .message("#This entity does not exists!!").errorCode(4043).build();
        if (log.isDebugEnabled())
            responseDto.setDetails(ex.getMessage());
        return new ResponseEntity<>(responseDto, responseDto.getHttpStatus());
    }

    @ExceptionHandler(value = {DataIntegrityViolationException.class})
    public ResponseEntity<ResponseDto<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {

        ResponseDto<Void> responseDto = ResponseDto.<Void>builder().httpStatus(HttpStatus.BAD_REQUEST)
                .message("#Unique index or primary key violation!").errorCode(4005).build();
        if (log.isDebugEnabled())
            responseDto.setDetails(ex.getMessage() );
        return new ResponseEntity<>(responseDto, responseDto.getHttpStatus());
    }

    @ExceptionHandler(value = {BadRequestException.class, StockNotFoundException.class})
    public ResponseEntity<ResponseDto<Void>> handleException(AbstractThrowable ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(toDto(ex));
    }

    private ResponseDto<Void> toDto(AbstractThrowable exception) {
        ResponseDto<Void> dto = new ResponseDto<>();
        dto.setHttpStatus(exception.getHttpStatus());
        dto.setMessage(exception.getMessage());
        dto.setErrorCode(exception.getErrorCode());
        return dto;
    }

}
