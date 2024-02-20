package com.piwo.cryptoApp.exception;

public class TokenNotFoundException extends RuntimeException {
    private final String tokenNumber;

    public TokenNotFoundException(String tokenNumber) {
        super("Token with number: " + tokenNumber + " was not found");
        this.tokenNumber = tokenNumber;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }
}
