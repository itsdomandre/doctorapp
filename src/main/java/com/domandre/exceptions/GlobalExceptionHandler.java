package com.domandre.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
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

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("AUDIT: Failed login attempt — bad credentials");
        return new ResponseEntity<>("Invalid Credentials. Please Try again.", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>("Resource requested not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DateTimeRequestIsNotPermittedException.class)
    public ResponseEntity<String> handleDateTimeRequestIsNotPermittedException(DateTimeRequestIsNotPermittedException ex) {
        return new ResponseEntity<>("Requested Date/Time is not available", HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(AppointmentNotAprovedException.class)
    public ResponseEntity<String> handleAppointmentNotApprovedException(AppointmentNotAprovedException ex) {
        return new ResponseEntity<>("Anamnesis can only created for approved appointment", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AnamnesisAlreadyExistsException.class)
    public ResponseEntity<String> handleAnamnesisAlreadyExistsException(AnamnesisAlreadyExistsException ex) {
        return new ResponseEntity<>("Patient already has an anamnesis record.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DoctorMustBeProvidedException.class)
    public ResponseEntity<String> handleDoctorMustBeProvidedException(DoctorMustBeProvidedException ex) {
        return new ResponseEntity<>("A doctor must be assigned when approving an appointment", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientPermissionsException.class)
    public ResponseEntity<String> handleInsufficientPermissionsException(InsufficientPermissionsException ex) {
        log.warn("AUDIT: Unauthorized resource access attempt");
        return new ResponseEntity<>("Insufficient Permissions, please check again", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParseError(HttpMessageNotReadableException ex) {
        String message = "Invalid request format: verify the fields";

        if (ex.getMostSpecificCause().getMessage().contains("Cannot deserialize value of type")) {
            message = "Invalid or missing field. Please verify all mandatory fields.";
        }
        return new ResponseEntity<>(
                Map.of("error", message),
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidTokenException(InvalidTokenException ex) {
        return new ResponseEntity<>("Invalid or expired token", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<String> handlePasswordMismatchException(PasswordMismatchException ex) {
        return new ResponseEntity<>("New password and confirm do not match", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<String> handleAccountNotVerifiedException(AccountNotVerifiedException ex) {
        log.warn("AUDIT: Login attempt for unverified account");
        return new ResponseEntity<>("Account not verified. Please check your email to activate your account.", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AppointmentNotCancellableException.class)
    public ResponseEntity<String> handleAppointmentNotCancellableException(AppointmentNotCancellableException ex) {
        return new ResponseEntity<>("Appointment cannot be cancelled in its current status.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EmailIntegrationErrorException.class)
    public ResponseEntity<String> handleEmailIntegrationErrorException(EmailIntegrationErrorException ex) {
        return new ResponseEntity<>("Failed to send email. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
    }

}
