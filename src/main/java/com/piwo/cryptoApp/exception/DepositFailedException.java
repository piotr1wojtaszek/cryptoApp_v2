package com.piwo.cryptoApp.exception;

public class DepositFailedException extends RuntimeException {

    public DepositFailedException() {
        super("Deposit failed. Please try again later.");
    }
}
