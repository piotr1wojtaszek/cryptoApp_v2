package com.piwo.cryptoApp.exception;

public class ExpiredTokenException extends Throwable {
    public ExpiredTokenException() {
        super("Token has expired!");
    }
}
