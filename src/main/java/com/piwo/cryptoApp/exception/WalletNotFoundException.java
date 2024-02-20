package com.piwo.cryptoApp.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(Long walletId) {
        super("Wallet not found with id: " + walletId);

    }
}
