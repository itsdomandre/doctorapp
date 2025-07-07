package com.domandre.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }
    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return new ResponseEntity<>("User Already Exists.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleForbiddenException(ForbiddenException ex) {
        return new ResponseEntity<>("User has no privileges to access this action.", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseEntity<>("Invalid Credentials. Please Try again.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>("Resource requested not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DateTimeRequestIsNotPermittedException.class)
    public ResponseEntity<String> handleDateTimeRequestIsNotPermittedException(DateTimeRequestIsNotPermittedException ex) {
        return new ResponseEntity<>("Requested Date/Time is occupied", HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(NoAppointmentsTodayException.class)
    public ResponseEntity<String> handleNoAppointmentsException(NoAppointmentsTodayException ex) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(AppointmentNotAprovedException.class)
    public ResponseEntity<String> handleAppointmentNotApprovedException(AppointmentNotAprovedException ex) {
        return new ResponseEntity<>("Anamnesis can only created for approved appointment", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AnamnesisAlreadyExistsException.class)
    public ResponseEntity<String> handleAnamnesisAlreadyExistsException(AnamnesisAlreadyExistsException ex) {
        return new ResponseEntity<>("Patient already has an anamnesis record.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParseError(HttpMessageNotReadableException ex) {
        String message = "Invalid request format: Ex.: 2020-01-04" ;
        return new ResponseEntity<>(
                Map.of("error", message),
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST
        );
    }

}
