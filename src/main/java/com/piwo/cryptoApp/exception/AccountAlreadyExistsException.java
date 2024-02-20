package com.piwo.cryptoApp.exception;

public class AccountAlreadyExistsException extends Throwable {
    public AccountAlreadyExistsException(String email) {
        super("Account with email already exists: "+email);
    }
}
