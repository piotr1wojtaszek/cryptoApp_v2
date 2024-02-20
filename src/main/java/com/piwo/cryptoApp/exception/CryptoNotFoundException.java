package com.piwo.cryptoApp.exception;

public class CryptoNotFoundException extends RuntimeException {
    public CryptoNotFoundException(String symbol) {
        super("Crypto not found with symbol: " + symbol);
    }
}
