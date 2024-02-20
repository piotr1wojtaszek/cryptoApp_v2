package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.WalletNotFoundException;
import com.piwo.cryptoApp.model.*;
import com.piwo.cryptoApp.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final AccountService accountService;

    /**
     * Returns wallet from database based on its id.
     * @param walletId wallet id.
     * @return returns wallet.
     * @throws WalletNotFoundException when walletId does not fit to any Wallet in database.
     */
    public Wallet getWallet(Long walletId) {
        return walletRepository.findById(walletId).orElseThrow(
                () -> new WalletNotFoundException(walletId));
    }

    /**
     * Returns List of crypto in wallet.
     * @param wallet wallet to check.
     * @return list of crypto.
     */
    public List<WalletCrypto> getWalletCryptoList(Wallet wallet) {
        return wallet.getWalletCryptoList();
    }

    /**
     * Returns the history of transactions related to the wallet.
     * @param wallet wallet to check.
     * @return list of transactions related to the wallet.
     */
    public List<Trade> getTransactionHistory(Wallet wallet) {
        return wallet.getTransactionHistory();
    }

    /**
     * Add amount to balance for updating data.
     * @param amount amount in usdt.
     * @return updated wallet.
     * @throws AccountNotFoundException if account from authentication service not found an account.
     */
    public Wallet addToBalance(BigDecimal amount) throws AccountNotFoundException {
        Wallet walletFromAuth = getWalletFromAuth();
        BigDecimal balance = walletFromAuth.getBalance().add(amount);
        walletFromAuth.setBalance(balance);
        return walletRepository.save(walletFromAuth);
    }

//    public Wallet subtractFromBalance(BigDecimal amount) throws AccountNotFoundException {
//        Wallet walletFromAuth = getWalletFromAuth();
//        BigDecimal balanceBefore = walletFromAuth.getBalance();
//        BigDecimal balanceAfter = balanceBefore.subtract(amount);
//
//        System.out.println("Balance before: " + balanceBefore);
//        System.out.println("Amount: " + amount);
//        System.out.println("Balance after: " + balanceAfter);
//
//        walletFromAuth.setBalance(balanceAfter);
//        Wallet updatedWallet = walletRepository.save(walletFromAuth);
//        System.out.println("Updated wallet balance: " + updatedWallet.getBalance());
//
//        return updatedWallet;
//    }

    /**
     * recalculates balance of user wallet
     *
     * @return wallet with recalculated balance
     * @throws AccountNotFoundException when account was not found
     */
    public Wallet recalculateBalance() throws AccountNotFoundException {
        Wallet wallet = getWalletFromAuth();
        List<WalletCrypto> walletCryptoList = wallet.getWalletCryptoList();
        BigDecimal recalculatedBalance = walletCryptoList.stream()
                .map(WalletCrypto::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(18, RoundingMode.HALF_UP);
        wallet.setBalance(recalculatedBalance);
        return walletRepository.save(wallet);
    }

    /**
     * gives access to wallet of logged user
     *
     * @return wallet from logged-in user
     * @throws AccountNotFoundException when account was not found
     */
    public Wallet getWalletFromAuth() throws AccountNotFoundException {
        Account authenticatedAccount = accountService.getAuthenticatedAccount();
        Wallet wallet = authenticatedAccount.getWallet();
        return getWallet(wallet.getId());
    }
}
