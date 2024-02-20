package com.piwo.cryptoApp.controller;

import com.piwo.cryptoApp.model.Trade;
import com.piwo.cryptoApp.model.Wallet;
import com.piwo.cryptoApp.model.WalletCrypto;
import com.piwo.cryptoApp.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cryptoApp/wallet")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{walletId}")
    public Wallet getWallet(@PathVariable Long walletId)  {
        return walletService.getWallet(walletId);
    }

    @GetMapping("/{walletId}/coins")
    public List<WalletCrypto> getWalletCryptoList(@PathVariable Long walletId){
        Wallet wallet = walletService.getWallet(walletId);
        return walletService.getWalletCryptoList(wallet);
    }

    @GetMapping()
    public Wallet getWalletFromAuthUser() throws AccountNotFoundException {
        return walletService.getWalletFromAuth();
    }

    @GetMapping("/{walletId}/transactions")
    public List<Trade> getTransactionHistory(@PathVariable Long walletId)  {
        Wallet wallet = walletService.getWallet(walletId);
        return walletService.getTransactionHistory(wallet);
    }
}
