package com.loginsystem.loginsystem.exception;

public class UserAlredyExistsException extends RuntimeException {

    public UserAlredyExistsException(String message){
        super(message);
    }
}
