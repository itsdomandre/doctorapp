package com.domandre.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return new ResponseEntity<>("User Already Exists.", HttpStatus.CONFLICT);
    }
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleForbiddenException(ForbiddenException ex) {
        return new ResponseEntity<>("User has no privileges to access this action.", HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?>handleBadCredentialsException (BadCredentialsException ex){
        return new ResponseEntity<>("Invalid Credentials. Please Try again.",HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>("Resource requested not found", HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(DateTimeRequestIsNotPermittedException.class)
    public ResponseEntity<String> handleDateTimeRequestIsNotPermittedException(ResourceNotFoundException ex) {
        return new ResponseEntity<>("Requested Date/Time is not in the range", HttpStatus.NOT_ACCEPTABLE);
    }
    @ExceptionHandler(NoAppointmentsTodayException.class)
    public ResponseEntity<String> handleNoAppointmentsException(NoAppointmentsTodayException ex) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
