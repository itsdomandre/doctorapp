package com.domandre.exceptions;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String s) {
        super(s);
    }

    public UserNotFoundException() {
    }
}