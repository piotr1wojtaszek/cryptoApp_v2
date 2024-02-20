package com.piwo.cryptoApp.exception;

public class WithdrawalFailedException extends RuntimeException {

    public WithdrawalFailedException(String message) {
        super("Withdrawal failed: "+message);
    }
}
