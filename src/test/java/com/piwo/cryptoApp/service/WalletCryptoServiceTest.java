package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.WalletCryptoNotFoundException;
import com.piwo.cryptoApp.exception.WalletNotFoundException;
import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.Crypto;
import com.piwo.cryptoApp.model.Wallet;
import com.piwo.cryptoApp.model.WalletCrypto;
import com.piwo.cryptoApp.repository.WalletCryptoRepository;
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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletCryptoServiceTest {

    @Mock
    private CryptoService cryptoService;
    @Mock
    private AccountService accountService;
    @Mock
    private WalletCryptoRepository walletCryptoRepository;
    @InjectMocks
    private WalletCryptoService walletCryptoService;

    @Test
    public void getWalletCrypto_returnsWalletCrypto() {
        //given
        Long walletCryptoId = 1L;
        WalletCrypto walletCrypto = new WalletCrypto(1L, new Crypto(), new BigDecimal(1), new BigDecimal(10));
        when(walletCryptoRepository.findById(walletCryptoId)).thenReturn(Optional.of(walletCrypto));
        //when
        WalletCrypto result = walletCryptoService.getWalletCrypto(walletCryptoId);
        //then
        assertThat(result, equalTo(walletCrypto));
    }

    @Test
    public void getWalletCrypto_throwśWalletCryptoNotFoundException() {
        //given
        Long walletCryptoId = 100L;
        when(walletCryptoRepository.findById(walletCryptoId)).thenReturn(Optional.empty());
        //when/then
        assertThrows(WalletCryptoNotFoundException.class, () -> walletCryptoService.getWalletCrypto(walletCryptoId));
    }

    @Test
    public void createWalletCrypto_returnsCreatedWalletCrypto() {
        //given
        Crypto crypto = new Crypto(1L, "BTC", new BigDecimal(30000));
        BigDecimal amount = new BigDecimal("0.5");
        WalletCrypto walletCrypto = new WalletCrypto();
        when(walletCryptoRepository.save(any(WalletCrypto.class))).thenReturn(walletCrypto);
        //when
        WalletCrypto result = walletCryptoService.createWalletCrypto(crypto, amount);
        //then
        assertThat(result, equalTo(walletCrypto));
    }

    @Test
    public void updateWalletCrypto_savesUpdatedWalletCrypto() throws AccountNotFoundException {
        //given
        String symbol = "BTC";
        BigDecimal amount = new BigDecimal("0.25");
        BigDecimal currentPrice = new BigDecimal("32000");
        Account account = new Account();
        Wallet wallet = new Wallet();
        List<WalletCrypto> walletCryptoList = new ArrayList<>();

        Crypto crypto = new Crypto(1L, "BTC", new BigDecimal("30000"));
        WalletCrypto walletCrypto = new WalletCrypto();
        walletCrypto.setCrypto(crypto);
        walletCrypto.setAmount(new BigDecimal("0.3"));

        walletCryptoList.add(walletCrypto);
        wallet.setWalletCryptoList(walletCryptoList);
        account.setWallet(wallet);

        when(accountService.getAuthenticatedAccount()).thenReturn(account);
        when(walletCryptoRepository.save(any(WalletCrypto.class))).thenReturn(walletCrypto);
        when(cryptoService.getCrypto(symbol)).thenReturn(crypto);
        //when
        walletCryptoService.updateWalletCrypto(symbol, amount);
        //then
        verify(walletCryptoRepository, times(2)).save(walletCrypto);
        assertThat(walletCrypto.getAmount(), equalTo(new BigDecimal("0.55")));
    }

    @Test
    public void updateWalletCrypto_throwsAccountNotFoundException() throws AccountNotFoundException {
        //given
        String symbol = "BTC";
        BigDecimal amount = new BigDecimal("0.25");
        when(accountService.getAuthenticatedAccount()).thenThrow(AccountNotFoundException.class);
        //when/then
        assertThrows(AccountNotFoundException.class, () -> walletCryptoService.updateWalletCrypto(symbol, amount));
    }

    @Test
    public void save_success() {
        //given
        WalletCrypto walletCrypto = new WalletCrypto();
        //when
        walletCryptoService.save(walletCrypto);
        //then
        verify(walletCryptoRepository).save(walletCrypto);
    }

    @Test
    public void delete_success() {
        //given
        WalletCrypto walletCrypto = new WalletCrypto();
        //when
        walletCryptoService.delete(walletCrypto);
        //then
        verify(walletCryptoRepository).delete(walletCrypto);
    }


}