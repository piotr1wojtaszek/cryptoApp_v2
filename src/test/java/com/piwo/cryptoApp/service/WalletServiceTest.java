package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.WalletNotFoundException;
import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.Trade;
import com.piwo.cryptoApp.model.Wallet;
import com.piwo.cryptoApp.model.WalletCrypto;
import com.piwo.cryptoApp.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private AccountService accountService;
    @InjectMocks
    private WalletService walletService;

    @Test
    public void getWallet_returnsWallet() {
        //given
        Long walletId = 1L;
        Wallet wallet = new Wallet();
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        //when
        Wallet result = walletService.getWallet(walletId);
        //then
        assertThat(result, equalTo(wallet));
    }

    @Test
    public void getWallet_throwsWalletNotFoundException() {
        //given
        Long walletId = 1L;
        when(walletRepository.findById(walletId)).thenThrow(WalletNotFoundException.class);
        //when/then
        assertThrows(WalletNotFoundException.class, () -> walletService.getWallet(walletId));
    }

    @Test
    public void getWalletCryptoList_returnsListOfWalletCryptos() {
        //given
        Wallet wallet = new Wallet();
        List<WalletCrypto> walletCryptoList = new ArrayList<>();
        wallet.setWalletCryptoList(walletCryptoList);
        //when
        List<WalletCrypto> result = walletService.getWalletCryptoList(wallet);
        //then
        assertThat(result, equalTo(walletCryptoList));
    }

    @Test
    public void getTransactionHistory_returnsTransactionHistoryList() {
        //given
        Wallet wallet = new Wallet();
        List<Trade> transactionHistory = new ArrayList<>();
        wallet.setTransactionHistory(transactionHistory);
        //when
        List<Trade> result = walletService.getTransactionHistory(wallet);
        //then
        assertThat(result, equalTo(transactionHistory));
    }

    @Test
    public void addToBalance_returnsWalletWithUpdatedBalance() throws AccountNotFoundException {
        //given
        BigDecimal initialBalance = new BigDecimal("5");
        BigDecimal amountToAdd = new BigDecimal("10");
        Wallet wallet = new Wallet();
        Long walletId = 1L;
        wallet.setId(walletId);
        wallet.setBalance(initialBalance);
        Account authenticatedAccount = new Account();
        authenticatedAccount.setWallet(wallet);
        when(accountService.getAuthenticatedAccount()).thenReturn(authenticatedAccount);
        when(walletRepository.findById(any())).thenReturn(Optional.of(wallet));
        when(walletRepository.save(wallet)).thenReturn(wallet);

        //when
        Wallet result = walletService.addToBalance(amountToAdd);
        //then
        verify(walletRepository, times(1)).save(wallet);
        BigDecimal expectedBalance = initialBalance.add(amountToAdd);
        assertThat(result.getBalance(), equalTo(expectedBalance));
        assertThat(result, equalTo(wallet));
    }

    @Test
    public void addToBalance_throwsAccountNotFoundException() throws AccountNotFoundException {
        //given
        BigDecimal amountToAdd = new BigDecimal("10");
        Wallet wallet = new Wallet();
        when(accountService.getAuthenticatedAccount()).thenThrow(AccountNotFoundException.class);
        //when/then
        assertThrows(AccountNotFoundException.class, () -> walletService.addToBalance(amountToAdd));
        verify(walletRepository, never()).save(wallet);

    }

    @Test
    public void recalculateBalance_returnsWalletWithUpdatedBalance() throws AccountNotFoundException {
        //given
        Wallet wallet = new Wallet();
        Long walletId = 1L;
        wallet.setId(walletId);
        List<WalletCrypto> walletCryptoList = new ArrayList<>();
        wallet.setWalletCryptoList(walletCryptoList);
        Account authenticatedAccount = new Account();
        authenticatedAccount.setWallet(wallet);
        when(accountService.getAuthenticatedAccount()).thenReturn(authenticatedAccount);
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any())).thenReturn(wallet);
        //when
        Wallet result = walletService.recalculateBalance();
        //then
        assertThat(result, equalTo(wallet));
    }

    @Test
    public void recalculateBalance_throwsAccountNotFoundException() throws AccountNotFoundException {
        //given
        Wallet wallet = new Wallet();
        when(accountService.getAuthenticatedAccount()).thenThrow(AccountNotFoundException.class);
        //when/then
        assertThrows(AccountNotFoundException.class, () -> walletService.recalculateBalance());
        verify(walletRepository, never()).save(wallet);
    }

    @Test
    public void getWalletFromAuth_returnsAccountsWallet() throws AccountNotFoundException {
        //given
        Account account = new Account();
        Wallet wallet = new Wallet();
        Long walletId = 1L;
        wallet.setId(walletId);
        account.setWallet(wallet);
        when(accountService.getAuthenticatedAccount()).thenReturn(account);
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        //when
        Wallet result = walletService.getWalletFromAuth();
        //then
        assertThat(result, equalTo(wallet));
    }

    @Test
    public void getWalletFromAuth_throwsAccountNotFoundException() throws AccountNotFoundException {
        //given
        when(accountService.getAuthenticatedAccount()).thenThrow(AccountNotFoundException.class);
        //when/then
        assertThrows(AccountNotFoundException.class, () -> walletService.getWalletFromAuth());
    }

}