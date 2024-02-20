package com.piwo.cryptoApp.exception;

public class WalletAlreadyExistsException extends Throwable {
    public WalletAlreadyExistsException(String message) {
        super(message);
    }
}
