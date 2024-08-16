package com.taskmanagement.exception;


import com.taskmanagement.model.dto.ResponseError;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Обработчик исключений для контроллеров. */
@RestControllerAdvice
@Slf4j
public class ControllerExceptionHandler {
    private static final  String ERROR_404 = "Error 400: Bad request - ";

    /** Обработчик исключения InsufficientAuthenticationException. */
    @ExceptionHandler({InsufficientAuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseError handleInsufflicientException(Exception ex, WebRequest request) {
        return new ResponseError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * Обработчик исключения UnauthorizedException, BadCredentialsException, MalformedJwtException,
     * ExpiredJwtException.
     */
    @ExceptionHandler({
            UnauthorizedException.class,
            BadCredentialsException.class,
            MalformedJwtException.class,
            ExpiredJwtException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseError handleUnauthorizedException(Exception ex, WebRequest request) {
        String errorMessage = "Error 401: Unauthorized - " + ex.getMessage();
        log.error(errorMessage);
        return new ResponseError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /** Обработчик исключения MissingServletRequestParameterException. */
    @ExceptionHandler({MissingServletRequestParameterException.class, ResponseStatusException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleIllegalArgumentException(
            RuntimeException ex, WebRequest request) {
        String errorMessage = ERROR_404 + ex.getMessage();
        log.error(errorMessage);
        return new ResponseError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
    /** Обработчик исключения MethodArgumentNotValidException. */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleNullEnumArgument(
            MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ERROR_404 + ex.getBindingResult().getFieldError().getDefaultMessage();
        log.error(errorMessage);
        return new ResponseError(HttpStatus.BAD_REQUEST, ex.getBindingResult().getFieldError().getDefaultMessage());
    }
    /** Обработчик исключения MethodArgumentNotValidException. */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError handleIllegalEnumArgument(
            HttpMessageNotReadableException ex, WebRequest request) {
        String errorMessage = ERROR_404 + ex.getMessage();
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(errorMessage);
        matcher.find();
        String acceptedValues = matcher.group(0);


        log.error(errorMessage);
        return new ResponseError(HttpStatus.BAD_REQUEST, "Invalid request format:" + acceptedValues );
    }

    /** Обработчик исключения HttpRequestMethodNotSupportedException. */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseError handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String errorMessage = "Error 405: Method not supported - " + ex.getMessage();
        log.error(errorMessage);
        return new ResponseError(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    /** Обработчик исключения RuntimeException. */
    @ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseError handleAllExceptions(RuntimeException ex, WebRequest request) {
        String errorMessage = "Error 500: Internal server error - " + ex.getMessage();
        log.error(errorMessage);
        return new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }


    /**
     * Обработчик исключений UsernameNotFoundException, CountryNotFoundException,
     * PlayerNotFoundException.
     */
    @ExceptionHandler({
            UsernameNotFoundException.class,
            TaskNotFoundException.class,
            UserNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseError usernameNotFoundException(RuntimeException ex, WebRequest request) {
        String errorMessage = "Error 404: Not Found - " + ex.getMessage();
        log.error(errorMessage);
        return new ResponseError(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}
