package com.piwo.cryptoApp.exception;

public class WalletCryptoNotFoundException extends RuntimeException {
    public WalletCryptoNotFoundException(String SymbolOrID) {
        super("WalletCrypto not found with crypto: " + SymbolOrID);
    }
}
