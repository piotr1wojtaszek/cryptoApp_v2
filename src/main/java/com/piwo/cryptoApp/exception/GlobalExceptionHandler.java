package com.piwo.cryptoApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<String> handleAccountAlreadyExistsException(AccountAlreadyExistsException e) {
        return new ResponseEntity<>("Account exception: " + e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BuyCryptoException.class)
    public ResponseEntity<String> handleBuyCryptoException(BuyCryptoException e) {
        return new ResponseEntity<>("Transaction exception: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CryptoAlreadyExistsException.class)
    public ResponseEntity<String> handleCryptoAlreadyExistsException(CryptoAlreadyExistsException e) {
        return new ResponseEntity<>("Cryptocurrency exception: " + e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CryptoNotFoundException.class)
    public ResponseEntity<String> handleCryptoNotFoundException(CryptoNotFoundException e) {
        return new ResponseEntity<>("Cryptocurrency exception: " + e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DepositFailedException.class)
    public ResponseEntity<String> handleDepositFailedException(DepositFailedException e) {
        return new ResponseEntity<>("Deposit exception: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailAlreadyConfirmedException.class)
    public ResponseEntity<String> handleEmailAlreadyConfirmedException(EmailAlreadyConfirmedException e) {
        return new ResponseEntity<>("Registry exception: " + e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<String> handleExpiredTokenException(ExpiredTokenException e) {
        return new ResponseEntity<>("Registry exception: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidPriceException.class)
    public ResponseEntity<String> handleInvalidPriceException(InvalidPriceException e) {
        return new ResponseEntity<>("Transaction exception: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SellCryptoException.class)
    public ResponseEntity<String> handleSellCryptoException(SellCryptoException e) {
        return new ResponseEntity<>("Transaction exception: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<String> handleTokenNotFoundException(TokenNotFoundException e) {
        return new ResponseEntity<>("Registry exception: " + e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TradeNotFoundException.class)
    public ResponseEntity<String> handleTradeNotFound(TradeNotFoundException e) {
        return new ResponseEntity<>("Transaction exception: " + e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<String> handleWalletAlreadyExistsException(WalletAlreadyExistsException e) {
        return new ResponseEntity<>("Wallet Exception: " + e.getMessage(), HttpStatus.CONFLICT);
    }


    @ExceptionHandler(WalletCryptoNotFoundException.class)
    public ResponseEntity<String> handleWalletCryptoNotFoundException(WalletCryptoNotFoundException e) {
        return new ResponseEntity<>("Cryptocurrency in wallet exception: " + e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<String> handleWalletNotFoundException(WalletNotFoundException e) {
        return new ResponseEntity<>("Account wallet exception: " + e.getMessage(), HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(WithdrawalFailedException.class)
    public ResponseEntity<String> handleWithdrawalFailedException(WithdrawalFailedException e) {
        return new ResponseEntity<>("Deposit failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

}
