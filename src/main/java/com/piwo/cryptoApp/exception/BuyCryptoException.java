package com.piwo.cryptoApp.exception;

public class BuyCryptoException extends RuntimeException {
    public BuyCryptoException(String cryptoSymbol) {
        super("Insufficient " + cryptoSymbol + " amount");
    }
}
