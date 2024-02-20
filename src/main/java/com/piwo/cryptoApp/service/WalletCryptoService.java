package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.WalletCryptoNotFoundException;
import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.Crypto;
import com.piwo.cryptoApp.model.WalletCrypto;
import com.piwo.cryptoApp.repository.WalletCryptoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletCryptoService {
    private final WalletCryptoRepository walletCryptoRepository;
    private final CryptoService cryptoService;
    private final AccountService accountService;

    /**
     * Returns cryptocurrency from wallet by its id.
     * @param walletCryptoId id of cryptocurrency in wallet.
     * @return returns cryptocurrency in wallet.
     */
    public WalletCrypto getWalletCrypto(Long walletCryptoId) {
        return walletCryptoRepository.findById(walletCryptoId).orElseThrow(
                () -> new WalletCryptoNotFoundException("Not found WalletCryptoID: " + walletCryptoId));
    }
    /**
     * Returns cryptocurrency from wallet by its symbol.
     * @param cryptoSymbol symbol cryptocurrency in wallet.
     * @return returns cryptocurrency in wallet.
     */
    public WalletCrypto getWalletCryptoByCrypto(String cryptoSymbol) {
        Crypto crypto = cryptoService.getCrypto(cryptoSymbol);
        return walletCryptoRepository.findByCrypto(crypto).orElseThrow(
                () -> new WalletCryptoNotFoundException(crypto.getSymbol())
        );
    }

    /**
     * Creates new crypto in wallet.
     * @param crypto cryptocurrency to add in users wallet.
     * @param amount amount of cryptocurrency.
     * @return returns added cryptocurrency.
     */
    public WalletCrypto createWalletCrypto(Crypto crypto, BigDecimal amount) {
        WalletCrypto walletCrypto = WalletCrypto.builder()
                .crypto(crypto)
                .amount(amount)
                .value(amount.multiply(crypto.getPrice()))
                .build();

        return walletCryptoRepository.save(walletCrypto);
    }

    /**
     * Updates amount of cryptocurrency in wallet by its symbol
     * @param symbol cryptocurrency symbol
     * @param amount amount of cryptocurrency
     * @throws AccountNotFoundException if account does not exist.
     */
    public void updateWalletCrypto(String symbol, BigDecimal amount) throws AccountNotFoundException {
        Account authenticatedAccount = accountService.getAuthenticatedAccount();
        List<WalletCrypto> walletCryptoList = authenticatedAccount.getWallet().getWalletCryptoList();
        WalletCrypto walletCrypto = walletCryptoList.stream()
                .filter(wc -> wc.getCrypto().getSymbol().equals(symbol))
                .findFirst().orElseThrow();
        BigDecimal newAmount = walletCrypto.getAmount().add(amount);
        walletCrypto.setAmount(newAmount);

        BigDecimal currentPrice = cryptoService.getCrypto(symbol).getPrice();

        walletCryptoRepository.save(walletCrypto);
        recalculateTotalPrice(walletCrypto, newAmount, currentPrice);
        walletCryptoRepository.save(walletCrypto);
    }

    /**
     * Updates total price of crypto in wallet.
     * @param walletCrypto crypto in wallet.
     * @param amount crypto amount.
     * @param price unit price of crypto.
     */
    private void recalculateTotalPrice(WalletCrypto walletCrypto, BigDecimal amount, BigDecimal price) {
        BigDecimal totalPrice = amount.multiply(price).setScale(18, RoundingMode.HALF_UP);
        walletCrypto.setValue(totalPrice);
    }

    /**
     * Saves cryptocurrency from wallet in database.
     * @param walletCrypto crypto from wallet.
     */
    public void save(WalletCrypto walletCrypto) {
        walletCryptoRepository.save(walletCrypto);
    }

    /**
     * Deletes cryptocurrency from wallet from database.
     * @param walletCrypto  crypto from wallet.
     */
    public void delete(WalletCrypto walletCrypto) {
        walletCryptoRepository.delete(walletCrypto);
    }
}
