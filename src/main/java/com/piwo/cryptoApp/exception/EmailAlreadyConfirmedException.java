package com.piwo.cryptoApp.exception;

public class EmailAlreadyConfirmedException extends Throwable {
    public EmailAlreadyConfirmedException() {
        super("Email already confirmed");
    }
}
